package org.egov.wscalculation.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.tracer.model.CustomException;
import org.egov.wscalculation.config.WSCalculationConfiguration;
import org.egov.wscalculation.constants.WSCalculationConstant;
import org.egov.wscalculation.producer.WSCalculationProducer;
import org.egov.wscalculation.repository.ServiceRequestRepository;
import org.egov.wscalculation.repository.WSCalculationDao;
import org.egov.wscalculation.util.CalculatorUtil;
import org.egov.wscalculation.util.WSCalculationUtil;
import org.egov.wscalculation.web.models.AdhocTaxReq;
import org.egov.wscalculation.web.models.BillEstimation;
import org.egov.wscalculation.web.models.BillFailureNotificationObj;
import org.egov.wscalculation.web.models.BillFailureNotificationRequest;
import org.egov.wscalculation.web.models.BillingSlab;
import org.egov.wscalculation.web.models.Calculation;
import org.egov.wscalculation.web.models.CalculationCriteria;
import org.egov.wscalculation.web.models.CalculationReq;
import org.egov.wscalculation.web.models.Demand;
import org.egov.wscalculation.web.models.Property;
import org.egov.wscalculation.web.models.Slab;
import org.egov.wscalculation.web.models.TaxHeadCategory;
import org.egov.wscalculation.web.models.TaxHeadEstimate;
import org.egov.wscalculation.web.models.TaxHeadMaster;
import org.egov.wscalculation.web.models.WaterConnection;
import org.egov.wscalculation.web.models.WaterConnectionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.datatype.threetenbp.deser.LocalDateDeserializer;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Service
@Slf4j
public class WSCalculationServiceImpl implements WSCalculationService {

	@Autowired
	private PayService payService;

	@Autowired
	private EstimationService estimationService;

	@Autowired
	private CalculatorUtil calculatorUtil;

	@Autowired
	private DemandService demandService;

	@Autowired
	private MasterDataService masterDataService;

	@Autowired
	private WSCalculationDao wSCalculationDao;

	@Autowired
	private ServiceRequestRepository repository;

	@Autowired
	private WSCalculationUtil wSCalculationUtil;

	@Autowired
	private WSCalculationProducer producer;

	@Autowired
	private WSCalculationConfiguration config;

	@Autowired
	private CalculatorUtil calculatorUtils;

	@Autowired
	private WSCalculationProducer wsCalculationProducer;

	@Autowired
	private MasterDataService mDataService;
	
	@Value("${app.timezone}")
	private String timeZone="IST";
	

	/**
	 * Get CalculationReq and Calculate the Tax Head on Water Charge And Estimation
	 * Charge
	 */
	public List<Calculation> getCalculation(CalculationReq request) {
		List<Calculation> calculations;

		Map<String, Object> masterMap;
		if (request.getIsconnectionCalculation()) {
			// Calculate and create demand for connection
			masterMap = masterDataService.loadMasterData(request.getRequestInfo(),
					request.getCalculationCriteria().get(0).getTenantId());
			calculations = getCalculations(request, masterMap);
		} else {
			// Calculate and create demand for application
			masterMap = masterDataService.loadExemptionMaster(request.getRequestInfo(),
					request.getCalculationCriteria().get(0).getTenantId());
			calculations = getFeeCalculation(request, masterMap);
		}
		demandService.generateDemand(request.getRequestInfo(), calculations, masterMap,
				request.getIsconnectionCalculation());
		unsetWaterConnection(calculations);
		return calculations;
	}

	public double getBillMonthsToCharge(Map<String, Object> startAndEndDate) {
		Long billingCycleEndDate = (Long) startAndEndDate.get("endingDay");
		LocalDate billingPeriodEndDate = LocalDate.ofEpochDay(billingCycleEndDate / 86400000L);
		LocalDate toDay = LocalDate.now();
		Period difference = Period.between(toDay, billingPeriodEndDate);
		double monthsToCharge = difference.getMonths() + 1;
		return monthsToCharge;
	}
	
	
	public void generateDemandForNewModifiedConn(RequestInfo requestInfo,Long billingDate) {
		//List<String> tenantIds = wSCalculationDao.getTenantId();
		List<String> tenantIds = new ArrayList<String>();
		tenantIds.add("pb.testing");
		
		if (tenantIds.isEmpty())
			return;
		tenantIds.forEach(tenantId -> {
			demandService.generateDemandForForActivatedConn ( requestInfo,  tenantId,billingDate );
		});
	}

	public BillEstimation getBillEstimate(CalculationReq calcReq) {
		//Validate master data
		Map<String, Object> masterMap;
		try {
			 masterMap = mDataService.loadMasterData(calcReq.getRequestInfo(), calcReq.getCalculationCriteria().get(0).getTenantId());
		}catch (Exception e) {
				throw new CustomException("BILLING_MASTER_DATA_NOT_FOUND", "Billing master data not available");
		}
		//Enrich the request data 
		Calendar dateObject = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
		CalculationCriteria calculationCriteria = calcReq.getCalculationCriteria().get(0);
		calculationCriteria.setBillingDate(dateObject.getTimeInMillis());
		calculationCriteria.setLastReading(0.0d);
		calculationCriteria.setCurrentReading(1000.0d);
			
		List<CalculationCriteria> calculationCriteriaList = new ArrayList<>();
		calculationCriteriaList.add(calculationCriteria);
			
		BillEstimation billEstimation = new BillEstimation();
		getWaterBillEstimate(calcReq, masterMap,billEstimation);
		return billEstimation;
		
	}

	
	

	/**
	 * 
	 * 
	 * @param request - Calculation Request Object
	 * @return List of calculation.
	 */
	public List<Calculation> bulkDemandGeneration(CalculationReq request, Map<String, Object> masterMap) {
		List<Calculation> calculations = getCalculations(request, masterMap);
		demandService.generateDemand(request.getRequestInfo(), calculations, masterMap, true);
		return calculations;
	}
	
	public List<Calculation> getWaterBillEstimate(CalculationReq request, Map<String, Object> masterMap,BillEstimation billEstimation ) {
		List<Calculation> calculations = new ArrayList<>(request.getCalculationCriteria().size());
		 CalculationCriteria criteria= request.getCalculationCriteria().get(0);
			Map<String, List> estimationMap = estimationService.getEstimationMap(criteria, request.getRequestInfo(),					
					masterMap,billEstimation);
			
			ArrayList<?> billingFrequencyMap = (ArrayList<?>) masterMap
					.get(WSCalculationConstant.Billing_Period_Master);
			masterDataService.enrichBillingPeriod(criteria, billingFrequencyMap, masterMap);
			log.info("Billing Estimate before calculation "+billEstimation);
			Map<String, Object> billingPeriod =(Map<String, Object>) masterMap.get(WSCalculationConstant.BILLING_PERIOD);
			Long billingCycleStartdDate = (Long) billingPeriod.get(WSCalculationConstant.STARTING_DATE_APPLICABLES);
			Long billingCycleEndDate = (Long) billingPeriod.get(WSCalculationConstant.ENDING_DATE_APPLICABLES);
			billEstimation.setBillingDate(billingCycleStartdDate);
			billEstimation.setBillingCycleEndDate(billingCycleEndDate);
			
			if (criteria.getWaterConnection().getConnectionType().equals(WSCalculationConstant.nonMeterdConnection)) {
				Long activationDate = criteria.getBillingDate();
				billEstimation.setBillingDate(activationDate); 
				int totalMonth = getBillingMonthsToCharge(billingCycleStartdDate, billingCycleEndDate);
				int balanceMonth = getBillingMonthsToCharge(activationDate, billingCycleEndDate);
				billEstimation.setMonthsToCharge(balanceMonth);
				
				log.info("total month "+ totalMonth  +" balance month "+ balanceMonth);
				
				if(estimationMap.get("estimates")!=null) {
					List<TaxHeadEstimate> taxHeadEstimates=estimationMap.get("estimates");
					for (TaxHeadEstimate taxHeadEstimate : taxHeadEstimates) {
						if(taxHeadEstimate.getTaxHeadCode().equals(WSCalculationConstant.WS_CHARGE)) {
							BigDecimal finalAmount =taxHeadEstimate.getEstimateAmount().multiply(new BigDecimal(balanceMonth)).divide(new BigDecimal(totalMonth),2,2).setScale(2, 2);
							taxHeadEstimate.setEstimateAmount(finalAmount);
							billEstimation.setWaterCharge(finalAmount);
						}
					}
					estimationMap.put("estimates", taxHeadEstimates);
				}
			}
			log.info("Estimation updated "+  billEstimation);
			Calculation calculation = getCalculation(request.getRequestInfo(), criteria, estimationMap, masterMap,
					true);
			calculations.add(calculation);
		return calculations;
	}

	public int getBillingMonthsToCharge(Long fromDate , Long toDate) {
		Calendar m1 = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
		m1.setTimeInMillis(fromDate);
		Calendar m2 = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
		m2.setTimeInMillis(toDate);
	    return  (m2.get(Calendar.YEAR)*12 + m2.get(Calendar.MONTH))- (m1.get(Calendar.YEAR)*12 + m1.get(Calendar.MONTH))+1; 
	}
	
	/**
	 * 
	 * @param request - Calculation Request Object
	 * @return list of calculation based on request
	 */
	public List<Calculation> getEstimation(CalculationReq request) {
		Map<String, Object> masterData = masterDataService.loadExemptionMaster(request.getRequestInfo(),
				request.getCalculationCriteria().get(0).getTenantId());
		List<Calculation> calculations = getFeeCalculation(request, masterData);
		unsetWaterConnection(calculations);
		return calculations;
	}

	/**
	 * It will take calculation and return calculation with tax head code
	 * 
	 * @param requestInfo              Request Info Object
	 * @param criteria                 Calculation criteria on meter charge
	 * @param estimatesAndBillingSlabs Billing Slabs
	 * @param masterMap                Master MDMS Data
	 * @return Calculation With Tax head
	 */
	public Calculation getCalculation(RequestInfo requestInfo, CalculationCriteria criteria,
			Map<String, List> estimatesAndBillingSlabs, Map<String, Object> masterMap, boolean isConnectionFee) {

		@SuppressWarnings("unchecked")
		List<TaxHeadEstimate> estimates = estimatesAndBillingSlabs.get("estimates");
		@SuppressWarnings("unchecked")
		List<String> billingSlabIds = estimatesAndBillingSlabs.get("billingSlabIds");
		WaterConnection waterConnection = criteria.getWaterConnection();
		String tenantId = criteria.getTenantId();

		if (StringUtils.isEmpty(tenantId)) {
			Property property = wSCalculationUtil.getProperty(
					WaterConnectionRequest.builder().waterConnection(waterConnection).requestInfo(requestInfo).build());
			tenantId = property.getTenantId();
		}
		@SuppressWarnings("unchecked")
		Map<String, TaxHeadCategory> taxHeadCategoryMap = ((List<TaxHeadMaster>) masterMap
				.get(WSCalculationConstant.TAXHEADMASTER_MASTER_KEY)).stream()
						.collect(Collectors.toMap(TaxHeadMaster::getCode, TaxHeadMaster::getCategory,
								(OldValue, NewValue) -> NewValue));

		BigDecimal taxAmt = BigDecimal.ZERO;
		BigDecimal waterCharge = BigDecimal.ZERO;
		BigDecimal penalty = BigDecimal.ZERO;
		BigDecimal exemption = BigDecimal.ZERO;
		BigDecimal rebate = BigDecimal.ZERO;
		BigDecimal fee = BigDecimal.ZERO;

		for (TaxHeadEstimate estimate : estimates) {

			TaxHeadCategory category = taxHeadCategoryMap.get(estimate.getTaxHeadCode());
			estimate.setCategory(category);

			switch (category) {

			case CHARGES:
				waterCharge = waterCharge.add(estimate.getEstimateAmount());
				break;

			case PENALTY:
				penalty = penalty.add(estimate.getEstimateAmount());
				break;

			case REBATE:
				rebate = rebate.add(estimate.getEstimateAmount());
				break;

			case EXEMPTION:
				exemption = exemption.add(estimate.getEstimateAmount());
				break;
			case FEE:
				fee = fee.add(estimate.getEstimateAmount());
				break;
			default:
				taxAmt = taxAmt.add(estimate.getEstimateAmount());
				break;
			}
		}
		TaxHeadEstimate decimalEstimate = payService.roundOfDecimals(taxAmt.add(penalty).add(waterCharge).add(fee),
				rebate.add(exemption), isConnectionFee);
		if (null != decimalEstimate) {
			decimalEstimate.setCategory(taxHeadCategoryMap.get(decimalEstimate.getTaxHeadCode()));
			estimates.add(decimalEstimate);
			if (decimalEstimate.getEstimateAmount().compareTo(BigDecimal.ZERO) >= 0)
				taxAmt = taxAmt.add(decimalEstimate.getEstimateAmount());
			else
				rebate = rebate.add(decimalEstimate.getEstimateAmount());
		}

		BigDecimal totalAmount = taxAmt.add(penalty).add(rebate).add(exemption).add(waterCharge).add(fee);
		return Calculation.builder().totalAmount(totalAmount).taxAmount(taxAmt).penalty(penalty).exemption(exemption)
				.charge(waterCharge).fee(fee).waterConnection(waterConnection).rebate(rebate).tenantId(tenantId)
				.taxHeadEstimates(estimates).billingSlabIds(billingSlabIds).connectionNo(criteria.getConnectionNo())
				.applicationNO(criteria.getApplicationNo()).build();
	}

	/**
	 * 
	 * @param request   would be calculations request
	 * @param masterMap master data
	 * @return all calculations including water charge and taxhead on that
	 */
	public List<Calculation> getCalculations(CalculationReq request, Map<String, Object> masterMap) {
		List<Calculation> calculations = new ArrayList<>(request.getCalculationCriteria().size());
		for (CalculationCriteria criteria : request.getCalculationCriteria()) {
			BillEstimation billEstimation = new BillEstimation();
			Map<String, List> estimationMap = estimationService.getEstimationMap(criteria, request.getRequestInfo(),
					masterMap, billEstimation);
			ArrayList<?> billingFrequencyMap = (ArrayList<?>) masterMap
					.get(WSCalculationConstant.Billing_Period_Master);
			masterDataService.enrichBillingPeriod(criteria, billingFrequencyMap, masterMap);
			Calculation calculation = getCalculation(request.getRequestInfo(), criteria, estimationMap, masterMap,
					true);
			calculations.add(calculation);
		}
		return calculations;
	}

	@Override
	public void jobScheduler() {
		// TODO Auto-generated method stub
		ArrayList<String> tenantIds = wSCalculationDao.searchTenantIds();

		for (String tenantId : tenantIds) {
			RequestInfo requestInfo = new RequestInfo();
			User user = new User();
			user.setTenantId(tenantId);
			requestInfo.setUserInfo(user);
			String jsonPath = WSCalculationConstant.JSONPATH_ROOT_FOR_BilingPeriod;
			MdmsCriteriaReq mdmsCriteriaReq = calculatorUtil.getBillingFrequency(requestInfo, tenantId);
			StringBuilder url = calculatorUtil.getMdmsSearchUrl();
			Object res = repository.fetchResult(url, mdmsCriteriaReq);
			if (res == null) {
				throw new CustomException("MDMS_ERROR_FOR_BILLING_FREQUENCY",
						"ERROR IN FETCHING THE BILLING FREQUENCY");
			}
			ArrayList<?> mdmsResponse = JsonPath.read(res, jsonPath);
			getBillingPeriod(mdmsResponse, requestInfo, tenantId);
		}
	}

	@SuppressWarnings("unchecked")
	public void getBillingPeriod(ArrayList<?> mdmsResponse, RequestInfo requestInfo, String tenantId) {
		log.info("Billing Frequency Map" + mdmsResponse.toString());
		Map<String, Object> master = (Map<String, Object>) mdmsResponse.get(0);
		LocalDateTime demandStartingDate = LocalDateTime.now();
		Long demandGenerateDateMillis = (Long) master.get(WSCalculationConstant.Demand_Generate_Date_String);

		String connectionType = "Non-metred";

		if (demandStartingDate.getDayOfMonth() == (demandGenerateDateMillis) / 86400) {

			ArrayList<String> connectionNos = wSCalculationDao.searchConnectionNos(connectionType, tenantId);
			for (String connectionNo : connectionNos) {

				CalculationReq calculationReq = new CalculationReq();
				CalculationCriteria calculationCriteria = new CalculationCriteria();
				calculationCriteria.setTenantId(tenantId);
				calculationCriteria.setConnectionNo(connectionNo);

				List<CalculationCriteria> calculationCriteriaList = new ArrayList<>();
				calculationCriteriaList.add(calculationCriteria);

				calculationReq.setRequestInfo(requestInfo);
				calculationReq.setCalculationCriteria(calculationCriteriaList);
				calculationReq.setIsconnectionCalculation(true);
				getCalculation(calculationReq);

			}
		}
	}

	/**
	 * Generate Demand Based on Time (Monthly, Quarterly, Yearly)
	 */
	public void generateDemandBasedOnTimePeriod(RequestInfo requestInfo) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime date = LocalDateTime.now();
		log.info("Time schedule start for water demand generation on : " + date.format(dateTimeFormatter));
		List<String> tenantIds = wSCalculationDao.getTenantId();
		if (tenantIds.isEmpty())
			return;
		tenantIds.forEach(tenantId -> {
			demandService.generateDemandForTenantId(tenantId, requestInfo, null, true);
		});
	}

	/**
	 * Generate Demand Manually
	 */
	public void generateDemandBasedOnTimePeriod_manual(RequestInfo requestInfo, String tenantId,
			List<String> connectionnos) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime date = LocalDateTime.now();
		log.info("Time schedule start for water demand generation on : " + date.format(dateTimeFormatter));

		demandService.generateDemandForTenantId(tenantId, requestInfo, connectionnos, false);
	}

	/**
	 * 
	 * @param request   - Calculation Request Object
	 * @param masterMap - Master MDMS Data
	 * @return list of calculation based on estimation criteria
	 */
	List<Calculation> getFeeCalculation(CalculationReq request, Map<String, Object> masterMap) {
		List<Calculation> calculations = new ArrayList<>(request.getCalculationCriteria().size());
		for (CalculationCriteria criteria : request.getCalculationCriteria()) {
			Map<String, List> estimationMap = estimationService.getFeeEstimation(criteria, request.getRequestInfo(),
					masterMap);
			masterDataService.enrichBillingPeriodForFee(masterMap);
			Calculation calculation = getCalculation(request.getRequestInfo(), criteria, estimationMap, masterMap,
					false);
			calculations.add(calculation);
		}
		return calculations;
	}

	public void unsetWaterConnection(List<Calculation> calculation) {
		calculation.forEach(cal -> cal.setWaterConnection(null));
	}

	/**
	 * Add adhoc tax to demand
	 * 
	 * @param adhocTaxReq - Adhox Tax Request Object
	 * @return List of Calculation
	 */
	public List<Calculation> applyAdhocTax(AdhocTaxReq adhocTaxReq) {
		List<TaxHeadEstimate> estimates = new ArrayList<>();
		if (!(adhocTaxReq.getAdhocpenalty().compareTo(BigDecimal.ZERO) == 0))
			estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_TIME_ADHOC_PENALTY)
					.estimateAmount(adhocTaxReq.getAdhocpenalty().setScale(2, 2)).build());
		if (!(adhocTaxReq.getAdhocrebate().compareTo(BigDecimal.ZERO) == 0))
			estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_TIME_ADHOC_REBATE)
					.estimateAmount(adhocTaxReq.getAdhocrebate().setScale(2, 2).negate()).build());
		Calculation calculation = Calculation.builder()
				.tenantId(adhocTaxReq.getRequestInfo().getUserInfo().getTenantId())
				.applicationNO(adhocTaxReq.getDemandId()).taxHeadEstimates(estimates).build();
		List<Calculation> calculations = Collections.singletonList(calculation);
		return demandService.updateDemandForAdhocTax(adhocTaxReq.getRequestInfo(), calculations);
	}

	@Override
	public void checkFailedBills(RequestInfo requestInfo, Long fromDateSearch, Long toDateSearch, String tenantId,
			String connectionno) {
		// TODO Auto-generated method stub
		List<Demand> demands = demandService.getDemandForFailedBills(requestInfo, fromDateSearch, toDateSearch,
				tenantId, connectionno);
		List<BillFailureNotificationObj> billDtls = wSCalculationDao.getFailedBillDtl(tenantId, connectionno);
		List<BillFailureNotificationObj> filterDemand = new ArrayList<BillFailureNotificationObj>();
		if (demands.size() > 0 && billDtls.size() > 0) {
			demands.forEach(passedDemand -> {
				// String passedConsumer = billDtls.stream().filter(d ->
				// d.equals(passedDemand.getConsumerCode())).findAny().orElse(null);
				BillFailureNotificationObj passedConsumer = billDtls.stream()
						.filter(d -> d.getConnectionNo().equals(passedDemand.getConsumerCode())).findAny().orElse(null);
				if (passedConsumer != null)
					filterDemand.add(passedConsumer);
			});
		}

		if (filterDemand.size() > 0) {
			filterDemand.forEach(demandObj -> {
				BillFailureNotificationRequest billFailureNotificationRequest = new BillFailureNotificationRequest();
				demandObj.setStatus(WSCalculationConstant.WS_BILL_STATUS_SUCCESS);
				demandObj.setLastModifiedTime(System.currentTimeMillis());
				demandObj.setLastModifiedBy(requestInfo.getUserInfo().getName());
				billFailureNotificationRequest.setBillFailureNotificationObj(demandObj);
				log.info("Send update msg to ws-failedBill-topic  :" + billFailureNotificationRequest);
				producer.push(config.getUpdatewsFailedBillTopic(), billFailureNotificationRequest);
			});
		}
	}

}

package org.egov.wscalculation.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Period;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.wscalculation.constants.WSCalculationConstant;
import org.egov.wscalculation.web.models.BillEstimation;
import org.egov.wscalculation.web.models.BillingSlab;
import org.egov.wscalculation.web.models.Calculation;
import org.egov.wscalculation.web.models.CalculationCriteria;
import org.egov.wscalculation.web.models.CalculationReq;
import org.egov.wscalculation.web.models.Property;
import org.egov.wscalculation.web.models.RequestInfoWrapper;
import org.egov.wscalculation.web.models.RoadTypeEst;
import org.egov.wscalculation.web.models.SearchCriteria;
import org.egov.wscalculation.web.models.Slab;
import org.egov.wscalculation.web.models.TaxHeadEstimate;
import org.egov.wscalculation.web.models.Unit;
import org.egov.wscalculation.web.models.WaterConnection;
import org.egov.wscalculation.web.models.WaterConnectionRequest;
import org.egov.wscalculation.web.models.WsTaxHeads;
import org.egov.wscalculation.util.CalculatorUtil;
import org.egov.wscalculation.util.WSCalculationUtil;
import org.egov.wscalculation.util.WaterCessUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Service
@Slf4j
public class EstimationService {

	@Autowired
	private WaterCessUtil waterCessUtil;
	
	@Autowired
	private CalculatorUtil calculatorUtil;
	

	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private WSCalculationUtil wSCalculationUtil;
	
	@Value("${app.timezone}")
	private String timeZone;
	
	@Autowired
	WSCalculationServiceImpl wsCalculationServiceImpl;
	
	@Autowired
	private MasterDataService mDataService;

	/**
	 * Generates a List of Tax head estimates with tax head code, tax head
	 * category and the amount to be collected for the key.
	 *
	 * @param criteria
	 *            criteria based on which calculation will be done.
	 * @param requestInfo
	 *            request info from incoming request.
	 * @return Map<String, Double>
	 */
	
	//To Remove
	@SuppressWarnings("rawtypes")
	public Map<String, List> getEstimationMap_1(CalculationCriteria criteria, RequestInfo requestInfo,Map<String, Object> masterData) {
		
		String tenantId = null != criteria.getTenantId() ? criteria.getTenantId() : requestInfo.getUserInfo().getTenantId();	
		
		if (criteria.getWaterConnection() == null && !StringUtils.isEmpty(criteria.getConnectionNo())) {
			List<WaterConnection> waterConnectionList = calculatorUtil.getWaterConnection(requestInfo, criteria.getConnectionNo(), tenantId);
			WaterConnection waterConnection = calculatorUtil.getWaterConnectionObject(waterConnectionList);
			criteria.setWaterConnection(waterConnection);
		}
		if (criteria.getWaterConnection() == null || StringUtils.isEmpty(criteria.getConnectionNo())) {
			StringBuilder builder = new StringBuilder();
			builder.append("Water Connection are not present for ")
					.append(StringUtils.isEmpty(criteria.getConnectionNo()) ? "" : criteria.getConnectionNo())
					.append(" connection no");
			throw new CustomException("WATER_CONNECTION_NOT_FOUND", builder.toString());
		}
		
		
		
		Map<String, JSONArray> billingSlabMaster = new HashMap<>();
		Map<String, JSONArray> timeBasedExemptionMasterMap = new HashMap<>();
		ArrayList<String> billingSlabIds = new ArrayList<>();
		billingSlabMaster.put(WSCalculationConstant.WC_BILLING_SLAB_MASTER,
				(JSONArray) masterData.get(WSCalculationConstant.WC_BILLING_SLAB_MASTER));
		billingSlabMaster.put(WSCalculationConstant.CALCULATION_ATTRIBUTE_CONST,
				(JSONArray) masterData.get(WSCalculationConstant.CALCULATION_ATTRIBUTE_CONST));
		timeBasedExemptionMasterMap.put(WSCalculationConstant.WC_WATER_CESS_MASTER,
				(JSONArray) (masterData.getOrDefault(WSCalculationConstant.WC_WATER_CESS_MASTER, null)));
		// mDataService.setWaterConnectionMasterValues(requestInfo, tenantId,
		// billingSlabMaster,
		// timeBasedExemptionMasterMap);
		BigDecimal taxAmt = new BigDecimal(0);
		//BigDecimal taxAmt = getWaterEstimationCharge(criteria.getWaterConnection(), criteria, billingSlabMaster, billingSlabIds,requestInfo);
		List<TaxHeadEstimate> taxHeadEstimates = getEstimatesForTax(taxAmt, criteria.getWaterConnection(),
				timeBasedExemptionMasterMap, RequestInfoWrapper.builder().requestInfo(requestInfo).build());

		Map<String, List> estimatesAndBillingSlabs = new HashMap<>();
		estimatesAndBillingSlabs.put("estimates", taxHeadEstimates);
		// Billing slab id
		estimatesAndBillingSlabs.put("billingSlabIds", billingSlabIds);
		return estimatesAndBillingSlabs;
	}
	
	@SuppressWarnings("rawtypes")
	public Map<String, Object> getEstimationMap(CalculationCriteria criteria, RequestInfo requestInfo,Map<String, Object> masterData) {
		
		String tenantId = null != criteria.getTenantId() ? criteria.getTenantId() : requestInfo.getUserInfo().getTenantId();		
		
		
		if(criteria.getWaterConnection() == null) {
			if (criteria.getWaterConnection() == null && !StringUtils.isEmpty(criteria.getConnectionNo())) {
				List<WaterConnection> waterConnectionList = calculatorUtil.getWaterConnection(requestInfo, criteria.getConnectionNo(), tenantId);
				WaterConnection waterConnection = calculatorUtil.getWaterConnectionObject(waterConnectionList);
				criteria.setWaterConnection(waterConnection);
			}
			if (criteria.getWaterConnection() == null || StringUtils.isEmpty(criteria.getConnectionNo())) {
				StringBuilder builder = new StringBuilder();
				builder.append("Water Connection are not present for ")
						.append(StringUtils.isEmpty(criteria.getConnectionNo()) ? "" : criteria.getConnectionNo())
						.append(" connection no");
				throw new CustomException("WATER_CONNECTION_NOT_FOUND", builder.toString());
			}
			
		}
		
		
		Map<String, JSONArray> billingSlabMaster = new HashMap<>();
		Map<String, JSONArray> timeBasedExemptionMasterMap = new HashMap<>();
		ArrayList<String> billingSlabIds = new ArrayList<>();
		billingSlabMaster.put(WSCalculationConstant.WC_BILLING_SLAB_MASTER,
				(JSONArray) masterData.get(WSCalculationConstant.WC_BILLING_SLAB_MASTER));
		billingSlabMaster.put(WSCalculationConstant.CALCULATION_ATTRIBUTE_CONST,
				(JSONArray) masterData.get(WSCalculationConstant.CALCULATION_ATTRIBUTE_CONST));		
		
		JSONObject obj = mapper.convertValue(masterData.get(WSCalculationConstant.BILLING_PERIOD), JSONObject.class);		
		JSONArray billingPeriodArray = new JSONArray();
		billingPeriodArray.add(obj);		
		billingSlabMaster.put(WSCalculationConstant.BILLING_PERIOD, billingPeriodArray);
		
		timeBasedExemptionMasterMap.put(WSCalculationConstant.WC_WATER_CESS_MASTER,
				(JSONArray) (masterData.getOrDefault(WSCalculationConstant.WC_WATER_CESS_MASTER, null)));
		// mDataService.setWaterConnectionMasterValues(requestInfo, tenantId,
		// billingSlabMaster,
		// timeBasedExemptionMasterMap);
		
		Map<String,Object> restult = getBillSlabDetailsForBillGeneration(criteria.getWaterConnection(), criteria, billingSlabMaster, requestInfo);
		System.out.println("Got back the billing slab required, now pass for water calcultion");
		
		
				
		BillingSlab billingSlab = (BillingSlab) restult.get("BillingSlab");
		Double totalUOM = (Double) restult.get("TotalUOM");
		String caluclationAttrubute = (String) restult.get("CalculationAttribute");
		billingSlabIds.add(billingSlab.getId());
		System.out.println("My bill slab="+billingSlab.toString());
		
			
		BigDecimal taxAmt = getWaterEstimationCharge(criteria.getWaterConnection(), caluclationAttrubute, billingSlabMaster,billingSlab, totalUOM,requestInfo);
		List<TaxHeadEstimate> taxHeadEstimates = getEstimatesForTax(taxAmt, criteria.getWaterConnection(),
				timeBasedExemptionMasterMap, RequestInfoWrapper.builder().requestInfo(requestInfo).build());
		
		log.info("tax head eximate="+taxHeadEstimates.get(0).toString());

		//Map<String, Object> estimatesAndBillingSlabs = new HashMap<>();
		restult.put("estimates", taxHeadEstimates);
		// Billing slab id
		restult.put("billingSlabIds", billingSlabIds);
		return restult;
	}
	
	
	public BillEstimation getWaterChargeForEstimate(CalculationReq request,Map<String, Object> masterMap,Map<String,Object> calculationInput) {
		
		BillingSlab billingSlab = (BillingSlab) calculationInput.get("billingSlab");
		
		ArrayList<?> billingFrequencyMap = (ArrayList<?>) masterMap.get(WSCalculationConstant.Billing_Period_Master);
		mDataService.enrichBillingPeriod(request.getCalculationCriteria().get(0), billingFrequencyMap, masterMap);
		System.out.println("master map="+masterMap.get(WSCalculationConstant.BILLING_PERIOD));	
		
		Object billingPeriod =  masterMap.get(WSCalculationConstant.BILLING_PERIOD);		
		JSONObject billingPeriodObj = mapper.convertValue(billingPeriod, JSONObject.class);
		
		List<Calculation>calculationList = wsCalculationServiceImpl.getCalculationObj(calculationInput);
		Calculation calc = calculationList.get(0);
		String billingCycle = (String) billingPeriodObj.get("billingCycle");
		
		LocalDate toDay = LocalDate.now();	
		double monthsToCharge = 0.0;
		long billingCycleEndDate = (long) billingPeriodObj.get("endingDay");
		double billAmountForBillingPeriod=0;
	    double finalBillAmount = 0;
	    //LocalDate billingPeriodEndDate;
	    LocalDate billingPeriodEndDate = LocalDate.ofEpochDay(billingCycleEndDate / 86400000L);	
	    
	    System.out.println("Charging period="+toDay+":"+billingPeriodEndDate);
	    Period difference ;
	    BillEstimation billEstimation = new BillEstimation();
	    billEstimation.setBillingCycleEndDate(billingCycleEndDate);
		switch(billingCycle) {
		case(WSCalculationConstant.Monthly_Billing_Period) :
	    	  difference = Period.between(toDay, billingPeriodEndDate);
	    	  monthsToCharge = difference.getMonths()+1; 
	    	 finalBillAmount = ((calc.getTotalAmount().doubleValue())/12.0)*monthsToCharge;
	    	 billEstimation.setMonthsToCharge(monthsToCharge);    	 
	    	 
		  break;
		case(WSCalculationConstant.Quaterly_Billing_Period) :			
		    
  	  		difference = Period.between(toDay, billingPeriodEndDate);
  	  		monthsToCharge = difference.getMonths()+1; 		  
		    finalBillAmount = ((calc.getTotalAmount().doubleValue())/12.0)*monthsToCharge;
		    billEstimation.setMonthsToCharge(monthsToCharge);			
		   break;
		case(WSCalculationConstant.Yearly_Billing_Period) :
			
  	  		difference = Period.between(toDay, billingPeriodEndDate);
  	  		monthsToCharge = difference.getMonths()+1; 		 
		    finalBillAmount = ((calc.getTotalAmount().doubleValue())/12.0)*monthsToCharge;
		    billEstimation.setMonthsToCharge(monthsToCharge);
		   break;
		   
		case(WSCalculationConstant.Half_Yearly_Billing_Period) :							
  	  		difference = Period.between(toDay, billingPeriodEndDate);
  	  		monthsToCharge = difference.getMonths()+1; 		  
		    finalBillAmount = ((calc.getTotalAmount().doubleValue())/12.0)*monthsToCharge;				
		    billEstimation.setMonthsToCharge(monthsToCharge);		
		    break;
		   
		case(WSCalculationConstant.Bi_Monthly_Billing_Period) :
			
				difference = Period.between(toDay, billingPeriodEndDate);
	  	   		monthsToCharge = difference.getMonths()+1; 
			   // billAmountForBillingPeriod = (billAmountForBillingPeriod/12.0)*monthsToCharge;
			    finalBillAmount = ((calc.getTotalAmount().doubleValue())/12.0)*monthsToCharge;					
			    billEstimation.setMonthsToCharge(monthsToCharge);
		
		   break;
		  default :
			  Map<String, String> errorMap = new HashMap<>();
				errorMap.put("FEE_SLAB_NOT_FOUND", "Fee slab master data not found!!");
		 
		}
		billEstimation.setBillAmount(billAmountForBillingPeriod);
		billEstimation.setPayableBillAmount(finalBillAmount);
		billEstimation.setBillingSlab(billingSlab);
	
		
		return billEstimation;
		
	}
	
	
	public BillingSlab getFilteredBillingSlab(BillingSlab bl,String calculationAttribute,CalculationCriteria criteria,Property property) {	
		
				
		
		String attribute = bl.getCalculationAttribute();
		System.out.println("calculation attribut="+attribute);
		List<Slab> slabs = bl.getSlabs();			
		switch(attribute) {
		case WSCalculationConstant.propAreaConst :				
			
			try {
				
				if(property.getLandArea()!=null) {						
					double propertyAreaNeeded = property.getLandArea();
					Optional<Slab>req = slabs.parallelStream().filter(each -> each.getFrom() < propertyAreaNeeded &&  propertyAreaNeeded < each.getTo()).findFirst();
					List<Slab> matchingSlab = new ArrayList<Slab>();
					matchingSlab.add(req.get());
					bl.setSlabs(matchingSlab);
				}
				else {
					return null;
				}		
				
			} catch (Exception e) {
				throw new CustomException("PARSING_ERROR", "Billing Slab can not be parsed!");
			}
			break;
		case WSCalculationConstant.pipeSizeConst :				
			double pipeNeeded  = criteria.getWaterConnection().getPipeSize();
			Optional<Slab> req = slabs.stream().filter(each -> each.getFrom() < pipeNeeded && pipeNeeded < each.getTo()).findFirst();
			if(req.isPresent()) {		
				
				List<Slab> matchingSlab = new ArrayList<Slab>();
				matchingSlab.add(req.get());
				bl.setSlabs(matchingSlab);					
			}
			else {					
				return null;
				
			}	
			break;
		case WSCalculationConstant.arvConst:					
			
			try {
				
				if(property.getUnits().get(0).getArv().doubleValue() >0 ) {						
					double propertyARVNeeded = property.getUnits().get(0).getArv().doubleValue();
					Optional<Slab>requried = slabs.parallelStream().filter(each -> each.getFrom() < propertyARVNeeded &&  propertyARVNeeded < each.getTo()).findFirst();
					List<Slab> matchingSlab = new ArrayList<Slab>();
					matchingSlab.add(requried.get());
					bl.setSlabs(matchingSlab);
				}
				else {
					return null;
				}					
				
			} catch (Exception e) {
				throw new CustomException("PARSING_ERROR", "Billing Slab can not be parsed!");
			}
			break;	
			
			

		}
		return bl;
		
       }



	
	public BillingSlab getEstimationMapForApplicationNo(CalculationCriteria criteria, RequestInfo requestInfo,
			Map<String, Object> masterData) {
		Map<String, JSONArray> billingSlabMaster = new HashMap<>();
		Map<String, JSONArray> timeBasedExemptionMasterMap = new HashMap<>();
		ArrayList<String> billingSlabIds = new ArrayList<>();
		billingSlabMaster.put(WSCalculationConstant.WC_BILLING_SLAB_MASTER,
				(JSONArray) masterData.get(WSCalculationConstant.WC_BILLING_SLAB_MASTER));
		
		billingSlabMaster.put(WSCalculationConstant.CALCULATION_ATTRIBUTE_CONST,(JSONArray)masterData.get(WSCalculationConstant.CALCULATION_ATTRIBUTE_CONST));
		
		
		timeBasedExemptionMasterMap.put(WSCalculationConstant.WC_WATER_CESS_MASTER,(JSONArray) (masterData.getOrDefault(WSCalculationConstant.WC_WATER_CESS_MASTER, null)));
			
		JSONArray wsBillingSlab = billingSlabMaster.get("WCBillingSlab");
			
		if (billingSlabMaster.get(WSCalculationConstant.WC_BILLING_SLAB_MASTER) == null)
			throw new CustomException("BILLING_SLAB_NOT_FOUND", "Billing Slab are Empty");
		List<BillingSlab> mappingBillingSlab;
		Property propertytry =null;
		try {
			mappingBillingSlab = mapper.readValue(
					billingSlabMaster.get(WSCalculationConstant.WC_BILLING_SLAB_MASTER).toJSONString(),
					mapper.getTypeFactory().constructCollectionType(List.class, BillingSlab.class));
			propertytry = wSCalculationUtil.getProperty(WaterConnectionRequest.builder().waterConnection(criteria.getWaterConnection()).requestInfo(requestInfo).build());
		} catch (IOException e) {
			throw new CustomException("PARSING_ERROR", "Billing Slab can not be parsed!");
		}
	
		JSONObject calculationAttributeMaster = new JSONObject();
		calculationAttributeMaster.put(WSCalculationConstant.CALCULATION_ATTRIBUTE_CONST, billingSlabMaster.get(WSCalculationConstant.CALCULATION_ATTRIBUTE_CONST));
        String calculationAttribute = getCalculationAttribute(calculationAttributeMaster, criteria.getWaterConnection().getConnectionType());
		List<String> filterAttrs =getFilterAttribute(calculationAttributeMaster, criteria.getWaterConnection().getConnectionType());
		
		List<BillingSlab> billingSlabs = getSlabsFiltered(criteria.getWaterConnection(), mappingBillingSlab, calculationAttribute, filterAttrs, propertytry);
		if (billingSlabs == null || billingSlabs.isEmpty())
			throw new CustomException("BILLING_SLAB_NOT_FOUND", "Billing Slab are Empty");
		if (billingSlabs.size() > 1)
			throw new CustomException("INVALID_BILLING_SLAB",
					"More than one billing slab found");
		billingSlabIds.add(billingSlabs.get(0).getId());
		log.debug(" Billing Slab Id For Water Charge Calculation **********--->  " + billingSlabIds.toString());
		
	
		
		int index = Integer.parseInt(billingSlabIds.get(0));
		
	    Object obj = wsBillingSlab.get(index-1);	   	    
	    BillingSlab bl = mapper.convertValue(obj, BillingSlab.class);	    
	  
	    if(bl.getSlabs().size() >1) {
	    	
	    	JSONArray calculationAttr = (JSONArray) masterData.get(WSCalculationConstant.CALCULATION_ATTRIBUTE_CONST);
	    	
	    	JSONObject requiredBillingSlab = null;    	
	    	for(int i=0;i<calculationAttr.size();i++) {
	    		
	    		JSONObject eachJsonObj = mapper.convertValue(calculationAttr.get(i), JSONObject.class);
	    		if((eachJsonObj.get("active") .equals(true)) && eachJsonObj.get("name").equals(criteria.getWaterConnection().getConnectionType())) {
	    			
	    			requiredBillingSlab = eachJsonObj;
	    			break;
	    		}
	    	}
	    	
	    	
			//JSONObject jsonobj = mapper.convertValue(calculationAttr.get(0), JSONObject.class);
		
			String attribute = (String) requiredBillingSlab.get("attribute");
			List<Slab> slabs = bl.getSlabs();			
			switch(attribute) {
			case WSCalculationConstant.propAreaConst :				
				Property property =null;
				try {
					property = wSCalculationUtil.getProperty(WaterConnectionRequest.builder().waterConnection(criteria.getWaterConnection()).requestInfo(requestInfo).build());
					if(property.getLandArea()!=null) {						
						double propertyAreaNeeded = property.getLandArea();
						Optional<Slab>req = slabs.parallelStream().filter(each -> each.getFrom() < propertyAreaNeeded &&  propertyAreaNeeded < each.getTo()).findFirst();
						List<Slab> matchingSlab = new ArrayList<Slab>();
						matchingSlab.add(req.get());
						bl.setSlabs(matchingSlab);
					}
					else {
						return null;
					}		
					
				} catch (Exception e) {
					throw new CustomException("PARSING_ERROR", "Billing Slab can not be parsed!");
				}
				break;
			case WSCalculationConstant.pipeSizeConst :				
				double pipeNeeded  = criteria.getWaterConnection().getPipeSize();
				Optional<Slab> req = slabs.stream().filter(each -> each.getFrom() < pipeNeeded && pipeNeeded < each.getTo()).findFirst();
				if(req.isPresent()) {		
					
					List<Slab> matchingSlab = new ArrayList<Slab>();
					matchingSlab.add(req.get());
					bl.setSlabs(matchingSlab);					
				}
				else {					
					return null;
					
				}	
				break;
			case WSCalculationConstant.arvConst:					
				Property propertyARV =null;
				try {
					propertyARV = wSCalculationUtil.getProperty(WaterConnectionRequest.builder().waterConnection(criteria.getWaterConnection()).requestInfo(requestInfo).build());
					if(propertyARV.getUnits().get(0).getArv().doubleValue() >0 ) {						
						double propertyARVNeeded = propertyARV.getUnits().get(0).getArv().doubleValue();
						Optional<Slab>requried = slabs.parallelStream().filter(each -> each.getFrom() < propertyARVNeeded &&  propertyARVNeeded < each.getTo()).findFirst();
						List<Slab> matchingSlab = new ArrayList<Slab>();
						matchingSlab.add(requried.get());
						bl.setSlabs(matchingSlab);
					}
					else {
						return null;
					}					
					
				} catch (Exception e) {
					throw new CustomException("PARSING_ERROR", "Billing Slab can not be parsed!");
				}
				break;
				
				
				

			}
			
	    }

		return bl;
	}
	

	/**
	 * 
	 * @param waterCharge WaterCharge amount
	 * @param connection - Connection Object
	 * @param timeBasedExemptionsMasterMap List of Exemptions for the connection
	 * @param requestInfoWrapper - RequestInfo Wrapper object
	 * @return - Returns list of TaxHeadEstimates
	 */
	private List<TaxHeadEstimate> getEstimatesForTax(BigDecimal waterCharge,
			WaterConnection connection,
			Map<String, JSONArray> timeBasedExemptionsMasterMap, RequestInfoWrapper requestInfoWrapper) {
		List<TaxHeadEstimate> estimates = new ArrayList<>();
		// water_charge
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_CHARGE)
				.estimateAmount(waterCharge.setScale(2, 2)).build());

		// Water_cess
		if (timeBasedExemptionsMasterMap.get(WSCalculationConstant.WC_WATER_CESS_MASTER) != null) {
			List<Object> waterCessMasterList = timeBasedExemptionsMasterMap
					.get(WSCalculationConstant.WC_WATER_CESS_MASTER);
			BigDecimal waterCess;
			waterCess = waterCessUtil.getWaterCess(waterCharge, WSCalculationConstant.Assessment_Year, waterCessMasterList);
			estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_WATER_CESS)
					.estimateAmount(waterCess.setScale(2, 2)).build());
		}
		return estimates;
	}
	

	public BigDecimal getBillingMonthsToCharge(Long billingCycleEndDate) {
	    
	    LocalDate billingPeriodEndDate = LocalDate.ofEpochDay(billingCycleEndDate / 86400000L);		    
	    LocalDate toDay = LocalDate.now();		   
	    Period difference = Period.between(toDay, billingPeriodEndDate);
	    BigDecimal monthsToCharge = new BigDecimal(difference.getMonths()+1); 		    
	    return monthsToCharge;
}

	
	
	public Map<String,Object> getBillSlabDetailsForBillGeneration(WaterConnection waterConnection,CalculationCriteria criteria,Map<String, JSONArray> billingSlabMaster,RequestInfo requestInfo) {
		
		 Map<String,Object> result = new HashMap<String, Object>();
		
		if (billingSlabMaster.get(WSCalculationConstant.WC_BILLING_SLAB_MASTER) == null)
			throw new CustomException("BILLING_SLAB_NOT_FOUND", "Billing Slab are Empty");
		List<BillingSlab> mappingBillingSlab = null;
		Property property =null;
		try {
			mappingBillingSlab = mapper.readValue(
					billingSlabMaster.get(WSCalculationConstant.WC_BILLING_SLAB_MASTER).toJSONString(),
					mapper.getTypeFactory().constructCollectionType(List.class, BillingSlab.class));
			property = wSCalculationUtil.getProperty(
					WaterConnectionRequest.builder().waterConnection(waterConnection).requestInfo(requestInfo).build());
		}catch (IOException e) {
			// TODO: handle exception
		}
		JSONObject calculationAttributeMaster = new JSONObject();
		calculationAttributeMaster.put(WSCalculationConstant.CALCULATION_ATTRIBUTE_CONST, billingSlabMaster.get(WSCalculationConstant.CALCULATION_ATTRIBUTE_CONST));
        String calculationAttribute = getCalculationAttribute(calculationAttributeMaster, waterConnection.getConnectionType());
		List<String> filterAttrs =getFilterAttribute(calculationAttributeMaster, waterConnection.getConnectionType());
		
		List<BillingSlab> billingSlabs = getSlabsFiltered(waterConnection, mappingBillingSlab, calculationAttribute, filterAttrs, property);
		if (billingSlabs == null || billingSlabs.isEmpty())
			throw new CustomException("BILLING_SLAB_NOT_FOUND", "Billing Slab are Empty");
		if (billingSlabs.size() > 1)
			throw new CustomException("INVALID_BILLING_SLAB",
					"More than one billing slab found");
		//billingSlabIds.add(billingSlabs.get(0).getId());
		//log.debug(" Billing Slab Id For Water Charge Calculation --->  " + billingSlabIds.toString());
		
		BillingSlab billSlab = billingSlabs.get(0);
		System.out.println("Required billing slab="+billSlab);
		
		// WaterCharge Calculation
		Double  totalUOM = getUnitOfMeasurement(waterConnection, calculationAttribute, criteria,property);
		
		BillingSlab filteredBillingSlab = getFilteredBillingSlab(billSlab, calculationAttribute, criteria,property);
				
		result.put("BillingSlab", billSlab);
		result.put("TotalUOM",totalUOM);
		result.put("CalculationAttribute", calculationAttribute);
		result.put("FilteredBillingSlab", filteredBillingSlab);
		
		
		
		return result;
	}
	
	
	
	
	

	/**
	 * method to do a first level filtering on the slabs based on the values
	 * present in the Water Details
	 */

	/**
	 * @param waterConnection
	 * @param criteria
	 * @param billingSlabMaster
	 * @param billingSlabIds
	 * @param requestInfo
	 * @return
	 */
	public BigDecimal getWaterEstimationCharge(WaterConnection waterConnection, String calculationAttribute, 
			Map<String, JSONArray> billingSlabMaster,BillingSlab billSlab ,Double totalUOM, RequestInfo requestInfo) {
		
		
		BigDecimal waterCharge = BigDecimal.ZERO;
			

		
				
		// IF calculation type is flat then take flat rate else take slab and calculate the charge
		//For metered connection calculation on graded fee slab
		//For Non metered connection calculation on normal connection
	
		
		
		
		if (isRangeCalculation(calculationAttribute)) {
			if (totalUOM == 0.0)
				return waterCharge;
			if (waterConnection.getConnectionType().equalsIgnoreCase(WSCalculationConstant.meteredConnectionType) ||
					(waterConnection.getConnectionType().equalsIgnoreCase(WSCalculationConstant.nonMeterdConnection) && 
							billSlab.getCalculationAttribute().equalsIgnoreCase(WSCalculationConstant.noOfTapsConst)
							)) {
				for (Slab slab : billSlab.getSlabs()) {
					if (totalUOM > slab.getTo()) {
						waterCharge = waterCharge.add(BigDecimal.valueOf(((slab.getTo()) - (slab.getFrom())) * slab.getCharge()));
					} else if (totalUOM <= slab.getTo()) {
						waterCharge = waterCharge.add(BigDecimal.valueOf((totalUOM-slab.getFrom()) * slab.getCharge()));
						break;
					}
				}
				
				//New
				//waterCharge = waterCharge.divide(new BigDecimal(12), 2).multiply(monthsToCharge);			
				
				
				if (billSlab.getMinimumCharge() > waterCharge.doubleValue()) {
					waterCharge = BigDecimal.valueOf(billSlab.getMinimumCharge());
				}
				if (billSlab.getMaximumCharge() > 0 && billSlab.getMaximumCharge() < waterCharge.doubleValue()) {
					waterCharge = BigDecimal.valueOf(billSlab.getMaximumCharge());
				}
			}else if (waterConnection.getConnectionType()
					.equalsIgnoreCase(WSCalculationConstant.nonMeterdConnection)) {
				
				waterBillCalculationByBillCycle(billingSlabMaster)	;
				if(!CollectionUtils.isEmpty(billSlab.getSlabs())) {
				for (Slab slab : billSlab.getSlabs()) {
					System.out.println("Test?="+totalUOM+":"+slab.getCharge());
					if (totalUOM >= slab.getFrom() && totalUOM < slab.getTo()) {
						if(slab.getType()!=null && slab.getType().equalsIgnoreCase(WSCalculationConstant.CALC_TYPE_RATE)) {
							
							waterCharge = BigDecimal.valueOf((totalUOM * slab.getCharge()));
							System.out.println("Wataer charege11="+waterCharge);
							
						}else {
							waterCharge = BigDecimal.valueOf((slab.getCharge()));
							System.out.println("Wataer charege222="+waterCharge);
						}
						if (billSlab.getMinimumCharge() > waterCharge.doubleValue()) {
							waterCharge = BigDecimal.valueOf(billSlab.getMinimumCharge());
						}
						break;
					}
				}
				}else {
					waterCharge = BigDecimal.valueOf(billSlab.getMinimumCharge());
				}
				//New
				//waterCharge = waterCharge.divide(new BigDecimal(12), 2).multiply(monthsToCharge);		
			}
		} else {
			waterCharge = BigDecimal.valueOf(billSlab.getMinimumCharge());
			
		}

		//To handle with and without pump
		if ( !StringUtils.isEmpty( waterConnection.getMotorInfo()) && waterConnection.getMotorInfo().equalsIgnoreCase(WSCalculationConstant.WC_MOTOR_CONN) )
		{
		
			waterCharge = waterCharge.add(BigDecimal.valueOf(billSlab.getMotorCharge()));			
			
		}
		//To add maintenance charge
		if(billSlab.getMaintenanceCharge() != 0.0) {			
			waterCharge.add(new BigDecimal(billSlab.getMaintenanceCharge()));
		}
		
		
		System.out.println("Water charge reurning =="+waterCharge);
		
		return waterCharge;
	}
	
	
	public BigDecimal getWaterEstimationCharge_1(WaterConnection waterConnection, CalculationCriteria criteria, 
			Map<String, JSONArray> billingSlabMaster, ArrayList<String> billingSlabIds, RequestInfo requestInfo) {
		BigDecimal waterCharge = BigDecimal.ZERO;
		if (billingSlabMaster.get(WSCalculationConstant.WC_BILLING_SLAB_MASTER) == null)
			throw new CustomException("BILLING_SLAB_NOT_FOUND", "Billing Slab are Empty");
		List<BillingSlab> mappingBillingSlab;
		Property property =null;
		try {
			mappingBillingSlab = mapper.readValue(
					billingSlabMaster.get(WSCalculationConstant.WC_BILLING_SLAB_MASTER).toJSONString(),
					mapper.getTypeFactory().constructCollectionType(List.class, BillingSlab.class));
			property = wSCalculationUtil.getProperty(
					WaterConnectionRequest.builder().waterConnection(waterConnection).requestInfo(requestInfo).build());
		} catch (IOException e) {
			throw new CustomException("PARSING_ERROR", "Billing Slab can not be parsed!");
		}
		JSONObject calculationAttributeMaster = new JSONObject();
		calculationAttributeMaster.put(WSCalculationConstant.CALCULATION_ATTRIBUTE_CONST, billingSlabMaster.get(WSCalculationConstant.CALCULATION_ATTRIBUTE_CONST));
        String calculationAttribute = getCalculationAttribute(calculationAttributeMaster, waterConnection.getConnectionType());
		List<String> filterAttrs =getFilterAttribute(calculationAttributeMaster, waterConnection.getConnectionType());
		List<BillingSlab> billingSlabs = getSlabsFiltered(waterConnection, mappingBillingSlab, calculationAttribute, filterAttrs, property);
		if (billingSlabs == null || billingSlabs.isEmpty())
			throw new CustomException("BILLING_SLAB_NOT_FOUND", "Billing Slab are Empty");
		if (billingSlabs.size() > 1)
			throw new CustomException("INVALID_BILLING_SLAB",
					"More than one billing slab found");
		billingSlabIds.add(billingSlabs.get(0).getId());
		log.debug(" Billing Slab Id For Water Charge Calculation --->  " + billingSlabIds.toString());

		// WaterCharge Calculation
		 Double  totalUOM = getUnitOfMeasurement(waterConnection, calculationAttribute, criteria,property);
		
		BillingSlab billSlab = billingSlabs.get(0);
		System.out.println("Required billing slab="+billSlab);
		// IF calculation type is flat then take flat rate else take slab and calculate the charge
		//For metered connection calculation on graded fee slab
		//For Non metered connection calculation on normal connection

			
		
		
		
		
		if (isRangeCalculation(calculationAttribute)) {
			if (totalUOM == 0.0)
				return waterCharge;
			if (waterConnection.getConnectionType().equalsIgnoreCase(WSCalculationConstant.meteredConnectionType) ||
					(waterConnection.getConnectionType().equalsIgnoreCase(WSCalculationConstant.nonMeterdConnection) && 
							billSlab.getCalculationAttribute().equalsIgnoreCase(WSCalculationConstant.noOfTapsConst)
							)) {
				for (Slab slab : billSlab.getSlabs()) {
					if (totalUOM > slab.getTo()) {
						waterCharge = waterCharge.add(BigDecimal.valueOf(((slab.getTo()) - (slab.getFrom())) * slab.getCharge()));
					} else if (totalUOM <= slab.getTo()) {
						waterCharge = waterCharge.add(BigDecimal.valueOf((totalUOM-slab.getFrom()) * slab.getCharge()));
						break;
					}
				}
				
				//New
				//waterCharge = waterCharge.divide(new BigDecimal(12), 2).multiply(monthsToCharge);			
				
				
				if (billSlab.getMinimumCharge() > waterCharge.doubleValue()) {
					waterCharge = BigDecimal.valueOf(billSlab.getMinimumCharge());
				}
				if (billSlab.getMaximumCharge() > 0 && billSlab.getMaximumCharge() < waterCharge.doubleValue()) {
					waterCharge = BigDecimal.valueOf(billSlab.getMaximumCharge());
				}
			}else if (waterConnection.getConnectionType()
					.equalsIgnoreCase(WSCalculationConstant.nonMeterdConnection)) {
				
				waterBillCalculationByBillCycle(billingSlabMaster)	;
				if(!CollectionUtils.isEmpty(billSlab.getSlabs())) {
				for (Slab slab : billSlab.getSlabs()) {
					System.out.println("Test?="+totalUOM+":"+slab.getCharge());
					if (totalUOM >= slab.getFrom() && totalUOM < slab.getTo()) {
						if(slab.getType()!=null && slab.getType().equalsIgnoreCase(WSCalculationConstant.CALC_TYPE_RATE)) {
							
							waterCharge = BigDecimal.valueOf((totalUOM * slab.getCharge()));
							System.out.println("Wataer charege11="+waterCharge);
							
						}else {
							waterCharge = BigDecimal.valueOf((slab.getCharge()));
							System.out.println("Wataer charege222="+waterCharge);
						}
						if (billSlab.getMinimumCharge() > waterCharge.doubleValue()) {
							waterCharge = BigDecimal.valueOf(billSlab.getMinimumCharge());
						}
						break;
					}
				}
				}else {
					waterCharge = BigDecimal.valueOf(billSlab.getMinimumCharge());
				}
				//New
				//waterCharge = waterCharge.divide(new BigDecimal(12), 2).multiply(monthsToCharge);		
			}
		} else {
			waterCharge = BigDecimal.valueOf(billSlab.getMinimumCharge());
			
		}

		//To handle with and without pump
		if ( !StringUtils.isEmpty( waterConnection.getMotorInfo()) && waterConnection.getMotorInfo().equalsIgnoreCase(WSCalculationConstant.WC_MOTOR_CONN) )
		{
			
			//New motor
//			BigDecimal moterChargeForBillableMonths = BigDecimal.valueOf(billSlab.getMotorCharge()).divide(new BigDecimal(12), 2).multiply(monthsToCharge);	
//			waterCharge = waterCharge.add(moterChargeForBillableMonths);
			
			
			waterCharge = waterCharge.add(BigDecimal.valueOf(billSlab.getMotorCharge()));
			
			
		}
		//To add maintenance charge
		if(billSlab.getMaintenanceCharge() != 0.0) {
			//New maintanance
//			BigDecimal maintenanceChargeForBillableMonths = BigDecimal.valueOf(billSlab.getMaintenanceCharge()).divide(new BigDecimal(12), 2).multiply(monthsToCharge);	
//			waterCharge = waterCharge.add(maintenanceChargeForBillableMonths);
			
			waterCharge.add(new BigDecimal(billSlab.getMaintenanceCharge()));
		}
		
		
		System.out.println("Water charge reurning =="+waterCharge);
		
		return waterCharge;
	}
	
	
	public BigDecimal waterBillCalculationByBillCycle(Map<String, JSONArray> billingSlabMaster) {
		
		JSONArray test = billingSlabMaster.get(WSCalculationConstant.BILLING_PERIOD);
		return new BigDecimal(0);
	}
	
	
	
	
	private List<BillingSlab> getSlabsFiltered(WaterConnection waterConnection, List<BillingSlab> billingSlabs,
			String calculationAttribute,List<String> filterAttr, Property property) {
	
		final String connectionType = waterConnection.getConnectionType();
		//Based on Connection type and calculation attribute 
		billingSlabs= billingSlabs.stream().filter(slab -> {
			boolean isConnectionTypeMatching = slab.getConnectionType().equalsIgnoreCase(connectionType);
			boolean isCalculationAttributeMatching = slab.getCalculationAttribute()
					.equalsIgnoreCase(calculationAttribute);
			return  isConnectionTypeMatching && isCalculationAttributeMatching;
		}).collect(Collectors.toList());
		for (String filterName : filterAttr) {
			switch (filterName) {
			case "PropertyLocation":
				long propLocCount =0 ;
				if(!StringUtils.isEmpty(property.getAddress().getLocation())) {
					propLocCount=billingSlabs.stream().filter(slab -> { 
						return slab.getPropertyLocation().equalsIgnoreCase(property.getAddress().getLocation());
					}).count();
				}
				final String propLoc = propLocCount > 0 ?  property.getAddress().getLocation() :WSCalculationConstant.GENERIC_ATTRIBUTE;
				billingSlabs= billingSlabs.stream().filter(slab -> {
					return slab.getPropertyLocation().equalsIgnoreCase(propLoc); 
				}).collect(Collectors.toList());
				break;
			case "waterSource":
				long waterSourceCount =0 ;
				if(!StringUtils.isEmpty(waterConnection.getWaterSource())) {
					waterSourceCount=billingSlabs.stream().filter(slab -> { 
						return slab.getWaterSource().equalsIgnoreCase(waterConnection.getWaterSource());
					}).count();
				}
				final String waterSource = waterSourceCount > 0 ?  waterConnection.getWaterSource() :WSCalculationConstant.GENERIC_ATTRIBUTE;
				billingSlabs= billingSlabs.stream().filter(slab -> {
					return slab.getWaterSource().equalsIgnoreCase(waterSource); 
				}).collect(Collectors.toList());
				break;
			case "buildingType":			
				long buildTypeCount =0;
				if(!StringUtils.isEmpty(waterConnection.getUsageCategory())) {
					buildTypeCount =billingSlabs.stream().filter(slab -> { 
						return  slab.getBuildingType().equalsIgnoreCase(waterConnection.getUsageCategory()) ;
					}).count();
				}
				final String buildingType = buildTypeCount >0 ?   waterConnection.getUsageCategory()	: WSCalculationConstant.GENERIC_ATTRIBUTE;
				billingSlabs= billingSlabs.stream().filter(slab -> {
					return slab.getBuildingType().equalsIgnoreCase(buildingType);
				}).collect(Collectors.toList());
				break;
			case "buildingSubType":
				long buildSubTypeCount =0;
				if(!StringUtils.isEmpty(waterConnection.getSubUsageCategory())) {
					buildSubTypeCount =billingSlabs.stream().filter(slab -> { 
						return  slab.getBuildingSubType().equalsIgnoreCase(waterConnection.getSubUsageCategory()) ;
					}).count();
				}
				final String buildingSubType = buildSubTypeCount >0 ?   waterConnection.getSubUsageCategory()	: WSCalculationConstant.GENERIC_ATTRIBUTE;
				billingSlabs= billingSlabs.stream().filter(slab -> { 
					return slab.getBuildingSubType().equalsIgnoreCase(buildingSubType);
				}).collect(Collectors.toList());
				break; 
			case "motorInfo":
				long motorInfoCount =0;
				if(!StringUtils.isEmpty(waterConnection.getMotorInfo())) {
					motorInfoCount =billingSlabs.stream().filter(slab -> { 
						return  slab.getMotorInfo().equalsIgnoreCase(waterConnection.getMotorInfo()) ;
					}).count();
				}
				final String motorInfo = motorInfoCount >0 ?   waterConnection.getMotorInfo()	: WSCalculationConstant.GENERIC_ATTRIBUTE;
				billingSlabs= billingSlabs.stream().filter(slab -> { 
					return slab.getMotorInfo().equalsIgnoreCase(motorInfo);
				}).collect(Collectors.toList());
				break; 
			case "ownershipCategory": // eg:INSTITUTIONAL.PRIVATE etc..
				long ownershipCount =0 ;
				if(!StringUtils.isEmpty(property.getOwnershipCategory())) {
					ownershipCount =billingSlabs.stream().filter(slab -> {
						return  slab.getOwnershipCategory().equalsIgnoreCase(property.getOwnershipCategory());
					}).count();
				}
 				//If count is zero then change to generic category
				final String ownership = ownershipCount > 0 ? property.getOwnershipCategory() :  WSCalculationConstant.GENERIC_ATTRIBUTE;
				billingSlabs= billingSlabs.stream().filter(slab -> {
					return slab.getOwnershipCategory().equalsIgnoreCase(ownership);
				}).collect(Collectors.toList());
				break;	
			case "ownerType": //EG:-STAFF,FREEDOMFIGHTER etc.				
				String ownerType = null;
				if(!CollectionUtils.isEmpty(waterConnection.getConnectionHolders())) {
					ownerType = waterConnection.getConnectionHolders().get(0).getOwnerType();
				}else if(!CollectionUtils.isEmpty(property.getOwners())) {
					ownerType =  property.getOwners().get(0).getOwnerType();
				}
				final String ownerType_lambda = ownerType !=null ? ownerType :"" ;
				//Check if specific value exist for category
				long ownerTypeCount =billingSlabs.stream().filter(slab -> {
					boolean ownershipCategoryMatching = slab.getOwnerType().equalsIgnoreCase(ownerType_lambda);
					return  ownershipCategoryMatching;
				}).count();
				//If count is zero then change to generic category
				final String  ownerType_lambda2 = ownerTypeCount > 0 ? ownerType :  WSCalculationConstant.GENERIC_ATTRIBUTE;
				billingSlabs= billingSlabs.stream().filter(slab -> {
					boolean ownershipCategoryMatching = slab.getOwnerType().equalsIgnoreCase(ownerType_lambda2);
					return (ownershipCategoryMatching);
				}).collect(Collectors.toList());
				break;	
				
			case "propertyOwnershipCategory": //EG:-HOR , Tenant etc.
				final String  propertyOwnershipCategory =  !StringUtils.isEmpty(waterConnection.getPropertyOwnership()) ?waterConnection.getPropertyOwnership() :  "HOR";
				billingSlabs= billingSlabs.stream().filter(slab -> {
					boolean ownershipCategoryMatching = slab.getPropertyOwnershipCategory().equalsIgnoreCase(propertyOwnershipCategory);
					return (ownershipCategoryMatching);
				}).collect(Collectors.toList());
				break;	

			case "authorizedConnection": //EG:-HOR , Tenant etc.
				long authCount =0 ;
				if(!StringUtils.isEmpty(waterConnection.getAuthorizedConnection())) {
					authCount =billingSlabs.stream().filter(slab -> {
						return  slab.getAuthorizedConnection().equalsIgnoreCase(waterConnection.getAuthorizedConnection());
					}).count();
				}
				final String authConn = authCount > 0 ?  waterConnection.getAuthorizedConnection() :WSCalculationConstant.GENERIC_ATTRIBUTE;
				 billingSlabs= billingSlabs.stream().filter(slab -> {
					return slab.getAuthorizedConnection().equalsIgnoreCase(authConn); 
				}).collect(Collectors.toList());
				break;	
				
			default:
				log.info("INVALID USECASE "+ filterName);
				break;
			}
		}
		
		
		return billingSlabs;
	}
	
	private String getCalculationAttribute(Map<String, Object> calculationAttributeMap, String connectionType) {
		if (calculationAttributeMap == null)
			throw new CustomException("CALCULATION_ATTRIBUTE_MASTER_NOT_FOUND",
					"Calculation attribute master not found!!");
		JSONArray filteredMasters = JsonPath.read(calculationAttributeMap,
				"$.CalculationAttribute[?(@.name=='" + connectionType + "')]");
		JSONObject master = mapper.convertValue(filteredMasters.get(0), JSONObject.class);
		return master.getAsString(WSCalculationConstant.ATTRIBUTE);
	}
	
	
	private List<String> getFilterAttribute(Map<String, Object> calculationAttributeMap, String connectionType) {
		if (calculationAttributeMap == null)
			throw new CustomException("CALCULATION_ATTRIBUTE_MASTER_NOT_FOUND",
					"Calculation attribute master not found!!");
		JSONArray filteredMasters = JsonPath.read(calculationAttributeMap,
				"$.CalculationAttribute[?(@.name=='" + connectionType + "')]");
		JSONObject master = mapper.convertValue(filteredMasters.get(0), JSONObject.class);
		JSONArray array =mapper.convertValue(master.get(WSCalculationConstant.FILTER_ATTRIBUTE), JSONArray.class);
		List<String> arr =new ArrayList<String>();
		if(array!=null) {
			arr =array.stream().map(obj ->obj.toString()).collect(Collectors.toList());
		}
		
		return arr;
	}
	
	/**
	 * 
	 * @param type will be calculation Attribute
	 * @return true if calculation Attribute is not Flat else false
	 */
	private boolean isRangeCalculation(String type) {
		return !type.equalsIgnoreCase(WSCalculationConstant.flatRateCalculationAttribute);
	}
	
	public String getAssessmentYear(LocalDateTime localDateTime) {
		int currentMonth = localDateTime.getMonthValue();
		String assessmentYear;
		if (currentMonth >= Month.APRIL.getValue()) {
			assessmentYear = YearMonth.now().getYear() + "-";
			assessmentYear = assessmentYear
					+ (Integer.toString(YearMonth.now().getYear() + 1).substring(2, assessmentYear.length() - 1));
		} else {
			assessmentYear = YearMonth.now().getYear() - 1 + "-";
			assessmentYear = assessmentYear
					+ (Integer.toString(YearMonth.now().getYear()).substring(2, assessmentYear.length() - 1));

		}
		return assessmentYear;
	}
	
	public String getAssessmentYear() {
		return getAssessmentYear(LocalDateTime.now()); 
	}
	
	private Double getUnitOfMeasurement(WaterConnection waterConnection, String calculationAttribute,
			CalculationCriteria criteria,Property property) {
		Double totalUnit = 0.0;
		if (waterConnection.getConnectionType().equals(WSCalculationConstant.meteredConnectionType)) {
			totalUnit = (criteria.getCurrentReading() - criteria.getLastReading());
			totalUnit = totalUnit / 1000;
			totalUnit = (double)Math.round(totalUnit);		    
			return totalUnit;
		} else {  
			if(calculationAttribute.equalsIgnoreCase(WSCalculationConstant.noOfTapsConst)) {
				if (waterConnection.getNoOfTaps() == null)
					return totalUnit;
				return new Double(waterConnection.getNoOfTaps());
			}else if ( calculationAttribute.equalsIgnoreCase(WSCalculationConstant.pipeSizeConst)) {
				if (waterConnection.getPipeSize() == null)
					return totalUnit;
				return waterConnection.getPipeSize();
			}else if( calculationAttribute.equalsIgnoreCase(WSCalculationConstant.arvConst)) {
				if(!CollectionUtils.isEmpty(property.getUnits()) && property.getUnits().get(0).getArv()!=null) {
					return property.getUnits().get(0).getArv().doubleValue();
				}
				return totalUnit;
			}else if( calculationAttribute.equalsIgnoreCase(WSCalculationConstant.propAreaConst)) {
			if(property.getLandArea()!=null) {
				return property.getLandArea() ;
			}
			return totalUnit;		
		}
		}
		return 0.0;
	}
	
	
	public Map<String, Object> getHalfYearStartAndEndDate(Map<String, Object> billingPeriod){
		Calendar fromDateCalendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
		if(fromDateCalendar.get(Calendar.MONTH)<= Calendar.MARCH   ) {
			fromDateCalendar.set(Calendar.MONTH, Calendar.OCTOBER);	
			fromDateCalendar.add(Calendar.YEAR, -1);	
		}else if(fromDateCalendar.get(Calendar.MONTH)>=Calendar.OCTOBER   ) {
			fromDateCalendar.set(Calendar.MONTH, Calendar.OCTOBER);	
		}else {
			fromDateCalendar.set(Calendar.MONTH, Calendar.APRIL);	
		}
		fromDateCalendar.set(Calendar.DAY_OF_MONTH, 1); 
		setTimeToBeginningOfDay(fromDateCalendar);
		Calendar toDateCalendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
		toDateCalendar.setTimeInMillis(fromDateCalendar.getTimeInMillis());
		toDateCalendar.add(Calendar.MONTH, 5);
		toDateCalendar.set(Calendar.DAY_OF_MONTH, toDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		setTimeToEndofDay(toDateCalendar);
		billingPeriod.put(WSCalculationConstant.STARTING_DATE_APPLICABLES, fromDateCalendar.getTimeInMillis());
		billingPeriod.put(WSCalculationConstant.ENDING_DATE_APPLICABLES, toDateCalendar.getTimeInMillis());	
		return billingPeriod;
	}
	
	
	public Map<String, Object> getYearStartAndEndDate(Map<String, Object> billingPeriod){
		Calendar fromDateCalendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
		if(fromDateCalendar.get(Calendar.MONTH)< 3) {
			fromDateCalendar.add(Calendar.YEAR, -1);	
		}
		fromDateCalendar.set(Calendar.MONTH, Calendar.APRIL);
		fromDateCalendar.set(Calendar.DAY_OF_MONTH, 1); 
		setTimeToBeginningOfDay(fromDateCalendar);
		Calendar toDateCalendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
		toDateCalendar.setTimeInMillis(fromDateCalendar.getTimeInMillis());
		toDateCalendar.add(Calendar.YEAR, 1);
		toDateCalendar.add(Calendar.DAY_OF_MONTH, -1);
		setTimeToEndofDay(toDateCalendar);
		billingPeriod.put(WSCalculationConstant.STARTING_DATE_APPLICABLES, fromDateCalendar.getTimeInMillis());
		billingPeriod.put(WSCalculationConstant.ENDING_DATE_APPLICABLES, toDateCalendar.getTimeInMillis());	
		return billingPeriod;
	}
	
	public Map<String, Object> getQuarterStartAndEndDate(Map<String, Object> billingPeriod){
		Calendar fromDateCalendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
		fromDateCalendar.set(Calendar.MONTH, fromDateCalendar.get(Calendar.MONTH)/3 * 3);
		fromDateCalendar.set(Calendar.DAY_OF_MONTH, 1);
		setTimeToBeginningOfDay(fromDateCalendar);
		Calendar toDateCalendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
		toDateCalendar.set(Calendar.MONTH, toDateCalendar.get(Calendar.MONTH)/3 * 3 + 2);
		toDateCalendar.set(Calendar.DAY_OF_MONTH, toDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		setTimeToEndofDay(toDateCalendar);
		billingPeriod.put(WSCalculationConstant.STARTING_DATE_APPLICABLES, fromDateCalendar.getTimeInMillis());
		billingPeriod.put(WSCalculationConstant.ENDING_DATE_APPLICABLES, toDateCalendar.getTimeInMillis());
		return billingPeriod;
	}
	
	public Map<String, Object> getBiMonthStartAndEndDate(Map<String, Object> billingPeriod){
		Calendar fromDateCalendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));	
		fromDateCalendar.set(Calendar.MONTH, fromDateCalendar.get(Calendar.MONTH)/2 * 2);
		fromDateCalendar.set(Calendar.DAY_OF_MONTH, 1);
		setTimeToBeginningOfDay(fromDateCalendar);
		Calendar toDateCalendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
		toDateCalendar.set(Calendar.MONTH, toDateCalendar.get(Calendar.MONTH)/2 * 2 + 1);
		toDateCalendar.set(Calendar.DAY_OF_MONTH, toDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		setTimeToEndofDay(toDateCalendar);
		billingPeriod.put(WSCalculationConstant.STARTING_DATE_APPLICABLES, fromDateCalendar.getTimeInMillis());
		billingPeriod.put(WSCalculationConstant.ENDING_DATE_APPLICABLES, toDateCalendar.getTimeInMillis());
		return billingPeriod;
	}
	
	public Map<String, Object> getMonthStartAndEndDate(Map<String, Object> billingPeriod){
		Calendar monthStartDate = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
		monthStartDate.set(Calendar.DAY_OF_MONTH, monthStartDate.getActualMinimum(Calendar.DAY_OF_MONTH));
		setTimeToBeginningOfDay(monthStartDate);
	    
		Calendar monthEndDate = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
		monthEndDate.set(Calendar.DAY_OF_MONTH, monthEndDate.getActualMaximum(Calendar.DAY_OF_MONTH));
		setTimeToEndofDay(monthEndDate);
		billingPeriod.put(WSCalculationConstant.STARTING_DATE_APPLICABLES, monthStartDate.getTimeInMillis());
		billingPeriod.put(WSCalculationConstant.ENDING_DATE_APPLICABLES, monthEndDate.getTimeInMillis());
		return billingPeriod;
	}
	
	public static void setTimeToBeginningOfDay(Calendar calendar) {
	    calendar.set(Calendar.HOUR_OF_DAY, 0);
	    calendar.set(Calendar.MINUTE, 0);
	    calendar.set(Calendar.SECOND, 0);
	    calendar.set(Calendar.MILLISECOND, 0);
	}

	public static void setTimeToEndofDay(Calendar calendar) {
	    calendar.set(Calendar.HOUR_OF_DAY, 23);
	    calendar.set(Calendar.MINUTE, 59);
	    calendar.set(Calendar.SECOND, 59);
	    calendar.set(Calendar.MILLISECOND, 999);
	}
	
	
	/**
	 * 
	 * @param criteria - Calculation Search Criteria
	 * @param requestInfo - Request Info Object
	 * @param masterData - Master Data map
	 * @return Fee Estimation Map
	 */
	@SuppressWarnings("rawtypes")
	public Map<String, Object> getFeeEstimation(CalculationCriteria criteria, RequestInfo requestInfo,Map<String, Object> masterData) {
		if (StringUtils.isEmpty(criteria.getWaterConnection()) && !StringUtils.isEmpty(criteria.getApplicationNo())) {
			SearchCriteria searchCriteria = new SearchCriteria();
			searchCriteria.setApplicationNumber(criteria.getApplicationNo());
			searchCriteria.setTenantId(criteria.getTenantId());
			WaterConnection waterConnection = calculatorUtil.getWaterConnectionOnApplicationNO(requestInfo, searchCriteria, requestInfo.getUserInfo().getTenantId());
			criteria.setWaterConnection(waterConnection);
		}
		if (StringUtils.isEmpty(criteria.getWaterConnection())) {
			throw new CustomException("WATER_CONNECTION_NOT_FOUND",
					"Water Connection are not present for " + criteria.getApplicationNo() + " Application no");
		}
		ArrayList<String> billingSlabIds = new ArrayList<>();
		billingSlabIds.add("");
		//List<TaxHeadEstimate> taxHeadEstimates = getTaxHeadForFeeEstimation(criteria, masterData, requestInfo);
		List<TaxHeadEstimate> taxHeadEstimates = getTaxHeadForFeeEstimation1(criteria, requestInfo);
		Map<String, Object> estimatesAndBillingSlabs = new HashMap<>();
		estimatesAndBillingSlabs.put("estimates", taxHeadEstimates);
		// //Billing slab id
		estimatesAndBillingSlabs.put("billingSlabIds", billingSlabIds);
		return estimatesAndBillingSlabs;
	}
	
	private boolean isZero(BigDecimal fld) {
		return fld==null || fld.compareTo(BigDecimal.ZERO)==0 ? true : false;
	}
	private boolean isNotZero(BigDecimal fld) {
		return fld!=null && fld.compareTo(BigDecimal.ZERO)!=0 ? true : false;
	}
	
	private List<TaxHeadEstimate> getTaxHeadForFeeEstimation1(CalculationCriteria criteria,
			RequestInfo requestInfo) {
		List<TaxHeadEstimate> estimates = new ArrayList<>();
		WaterConnection connection = criteria.getWaterConnection();
		BigDecimal totalAmount = BigDecimal.ZERO;
		if (connection.getRoadTypeEst() != null && connection.getRoadTypeEst().size() != 0) {
			for (RoadTypeEst est : connection.getRoadTypeEst()) {
				BigDecimal length = est.getLength();
				BigDecimal breadth = est.getBreadth();
				BigDecimal depth = est.getDepth();
				BigDecimal rate = est.getRate();
				if(isNotZero(depth)|| isNotZero(breadth)  || isNotZero(length)  || isNotZero(rate)) {
					if(isZero(depth) || isZero(breadth)  || isZero(length)  || isZero(rate))
						throw new CustomException("Calculationa_attr","Please enter all the parameter(Length,Breadth,Depth & Rate) to calculate road cutting charges");
				}else { 
					length = BigDecimal.ZERO;
					breadth = BigDecimal.ZERO;
					depth = BigDecimal.ZERO;
					rate = BigDecimal.ZERO;
				}
				BigDecimal roadCuttingCharges = length.multiply(breadth).multiply(depth).multiply(rate).setScale(2, 2);
				totalAmount = totalAmount.add(roadCuttingCharges);
			}
		}
		if(connection.getWsTaxHeads()!=null && connection.getWsTaxHeads().size()!=0) {
			boolean flag = false;
			for (WsTaxHeads taxHeadEstimate : connection.getWsTaxHeads()) {
				if (taxHeadEstimate.getTaxHeadCode().equalsIgnoreCase("WS_ROAD_CUTTING_CHARGE")) {
					taxHeadEstimate.setAmount(totalAmount.setScale(2, 2));
					flag = true;
				}
				
				if(taxHeadEstimate.getAmount()!=null) {
				estimates.add(TaxHeadEstimate.builder().taxHeadCode(taxHeadEstimate.getTaxHeadCode())
						.estimateAmount(taxHeadEstimate.getAmount().setScale(2, 2)).build());
				}
				
			}
			if(!flag) {
				estimates.add(TaxHeadEstimate.builder().taxHeadCode("WS_ROAD_CUTTING_CHARGE")
						.estimateAmount(totalAmount.setScale(2, 2)).build());
			}
			
		}
		
		addAdhocPenaltyAndRebate(estimates, criteria.getWaterConnection());
		return estimates;
	}
	
	/**
	 * 
	 * @param criteria Calculation Search Criteria
	 * @param masterData - Master Data
	 * @param requestInfo - RequestInfo
	 * @return return all tax heads
	 */
	private List<TaxHeadEstimate> getTaxHeadForFeeEstimation(CalculationCriteria criteria,
			Map<String, Object> masterData, RequestInfo requestInfo) {
		JSONArray feeSlab = (JSONArray) masterData.getOrDefault(WSCalculationConstant.WC_FEESLAB_MASTER, null);
		if (feeSlab == null)
			throw new CustomException("FEE_SLAB_NOT_FOUND", "fee slab master data not found!!");
		
		Property property = wSCalculationUtil.getProperty(WaterConnectionRequest.builder()
				.waterConnection(criteria.getWaterConnection()).requestInfo(requestInfo).build());
		
		JSONObject feeObj = mapper.convertValue(feeSlab.get(0), JSONObject.class);
		BigDecimal formFee = BigDecimal.ZERO;
		if (feeObj.get(WSCalculationConstant.FORM_FEE_CONST) != null) {
			formFee = new BigDecimal(feeObj.getAsNumber(WSCalculationConstant.FORM_FEE_CONST).toString());
		}
		BigDecimal scrutinyFee = BigDecimal.ZERO;
		if (feeObj.get(WSCalculationConstant.SCRUTINY_FEE_CONST) != null) {
			scrutinyFee = new BigDecimal(feeObj.getAsNumber(WSCalculationConstant.SCRUTINY_FEE_CONST).toString());
		}
		BigDecimal otherCharges = BigDecimal.ZERO;
		if (feeObj.get(WSCalculationConstant.OTHER_CHARGE_CONST) != null) {
			otherCharges = new BigDecimal(feeObj.getAsNumber(WSCalculationConstant.OTHER_CHARGE_CONST).toString());
		}
		BigDecimal taxAndCessPercentage = BigDecimal.ZERO;
		if (feeObj.get(WSCalculationConstant.TAX_PERCENTAGE_CONST) != null) {
			taxAndCessPercentage = new BigDecimal(
					feeObj.getAsNumber(WSCalculationConstant.TAX_PERCENTAGE_CONST).toString());
		}
		BigDecimal meterCost = BigDecimal.ZERO;
		if (feeObj.get(WSCalculationConstant.METER_COST_CONST) != null
				&& criteria.getWaterConnection().getConnectionType() != null && criteria.getWaterConnection()
						.getConnectionType().equalsIgnoreCase(WSCalculationConstant.meteredConnectionType)) {
			meterCost = new BigDecimal(feeObj.getAsNumber(WSCalculationConstant.METER_COST_CONST).toString());
		}
		BigDecimal roadCuttingCharge = BigDecimal.ZERO;
		if (criteria.getWaterConnection().getRoadType() != null)
			roadCuttingCharge = getChargeForRoadCutting(masterData, criteria.getWaterConnection().getRoadType(),
					criteria.getWaterConnection().getRoadCuttingArea());
		BigDecimal roadPlotCharge = BigDecimal.ZERO;
		if (property.getLandArea() != null)
			roadPlotCharge = getPlotSizeFee(masterData, property.getLandArea());
		BigDecimal usageTypeCharge = BigDecimal.ZERO;
		if (criteria.getWaterConnection().getRoadCuttingArea() != null)
			usageTypeCharge = getUsageTypeFee(masterData,
					property.getUsageCategory(),
					criteria.getWaterConnection().getRoadCuttingArea());
		BigDecimal totalCharge = formFee.add(scrutinyFee).add(otherCharges).add(meterCost).add(roadCuttingCharge)
				.add(roadPlotCharge).add(usageTypeCharge);
		BigDecimal tax = totalCharge.multiply(taxAndCessPercentage.divide(WSCalculationConstant.HUNDRED));
		List<TaxHeadEstimate> estimates = new ArrayList<>();
		//
		if (!(formFee.compareTo(BigDecimal.ZERO) == 0))
			estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_FORM_FEE)
					.estimateAmount(formFee.setScale(2, 2)).build());
		if (!(scrutinyFee.compareTo(BigDecimal.ZERO) == 0))
			estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_SCRUTINY_FEE)
					.estimateAmount(scrutinyFee.setScale(2, 2)).build());
		if (!(meterCost.compareTo(BigDecimal.ZERO) == 0))
			estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_METER_CHARGE)
					.estimateAmount(meterCost.setScale(2, 2)).build());
		if (!(otherCharges.compareTo(BigDecimal.ZERO) == 0))
			estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_OTHER_CHARGE)
					.estimateAmount(otherCharges.setScale(2, 2)).build());
		if (!(roadCuttingCharge.compareTo(BigDecimal.ZERO) == 0))
			estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_ROAD_CUTTING_CHARGE)
					.estimateAmount(roadCuttingCharge.setScale(2, 2)).build());
		if (!(usageTypeCharge.compareTo(BigDecimal.ZERO) == 0))
			estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_ONE_TIME_FEE)
					.estimateAmount(usageTypeCharge.setScale(2, 2)).build());
		if (!(roadPlotCharge.compareTo(BigDecimal.ZERO) == 0))
			estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_SECURITY_CHARGE)
					.estimateAmount(roadPlotCharge.setScale(2, 2)).build());
		if (!(tax.compareTo(BigDecimal.ZERO) == 0))
			estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_TAX_AND_CESS)
					.estimateAmount(tax.setScale(2, 2)).build());
		addAdhocPenaltyAndRebate(estimates, criteria.getWaterConnection());
		return estimates;
	}
	
	/**
	 * 
	 * @param masterData Master Data Map
	 * @param roadType - Road type
	 * @param roadCuttingArea - Road Cutting Area
	 * @return road cutting charge
	 */
	private BigDecimal getChargeForRoadCutting(Map<String, Object> masterData, String roadType, Float roadCuttingArea) {
		JSONArray roadSlab = (JSONArray) masterData.getOrDefault(WSCalculationConstant.WC_ROADTYPE_MASTER, null);
		BigDecimal charge = BigDecimal.ZERO;
		JSONObject masterSlab = new JSONObject();
		if(roadSlab != null) {
			masterSlab.put("RoadType", roadSlab);
			JSONArray filteredMasters = JsonPath.read(masterSlab, "$.RoadType[?(@.code=='" + roadType + "')]");
			if (CollectionUtils.isEmpty(filteredMasters))
				return BigDecimal.ZERO;
			JSONObject master = mapper.convertValue(filteredMasters.get(0), JSONObject.class);
			charge = new BigDecimal(master.getAsNumber(WSCalculationConstant.UNIT_COST_CONST).toString());
			charge = charge.multiply(
					new BigDecimal(roadCuttingArea == null ? BigDecimal.ZERO.toString() : roadCuttingArea.toString()));
		}
		return charge;
	}
	
	/**
	 * 
	 * @param masterData - Master Data Map
	 * @param plotSize - Plot Size
	 * @return get fee based on plot size
	 */
	private BigDecimal getPlotSizeFee(Map<String, Object> masterData, Double plotSize) {
		BigDecimal charge = BigDecimal.ZERO;
		JSONArray plotSlab = (JSONArray) masterData.getOrDefault(WSCalculationConstant.WC_PLOTSLAB_MASTER, null);
		JSONObject masterSlab = new JSONObject();
		if (plotSlab != null) {
			masterSlab.put("PlotSizeSlab", plotSlab);
			JSONArray filteredMasters = JsonPath.read(masterSlab, "$.PlotSizeSlab[?(@.from <="+ plotSize +"&& @.to > " + plotSize +")]");
			if(CollectionUtils.isEmpty(filteredMasters))
				return charge;
			JSONObject master = mapper.convertValue(filteredMasters.get(0), JSONObject.class);
			charge = new BigDecimal(master.getAsNumber(WSCalculationConstant.UNIT_COST_CONST).toString());
		}
		return charge;
	}
	
	/**
	 * 
	 * @param masterData Master Data Map
	 * @param usageType - Property Usage Type
	 * @param roadCuttingArea Road Cutting Area
	 * @return  returns UsageType Fee
	 */
	private BigDecimal getUsageTypeFee(Map<String, Object> masterData, String usageType, Float roadCuttingArea) {
		BigDecimal charge = BigDecimal.ZERO;
		JSONArray usageSlab = (JSONArray) masterData.getOrDefault(WSCalculationConstant.WC_PROPERTYUSAGETYPE_MASTER, null);
		JSONObject masterSlab = new JSONObject();
		BigDecimal cuttingArea = new BigDecimal(roadCuttingArea.toString());
		if(usageSlab != null) {
			masterSlab.put("PropertyUsageType", usageSlab);
			JSONArray filteredMasters = JsonPath.read(masterSlab, "$.PropertyUsageType[?(@.code=='"+usageType+"')]");
			if(CollectionUtils.isEmpty(filteredMasters))
				return charge;
			JSONObject master = mapper.convertValue(filteredMasters.get(0), JSONObject.class);
			charge = new BigDecimal(master.getAsNumber(WSCalculationConstant.UNIT_COST_CONST).toString());
			charge = charge.multiply(cuttingArea);
		}
		return charge;
	}
	
	/**
	 * Enrich the adhoc penalty and adhoc rebate
	 * @param estimates tax head estimate
	 * @param connection water connection object
	 */
	@SuppressWarnings({ "unchecked"})
	private void addAdhocPenaltyAndRebate(List<TaxHeadEstimate> estimates, WaterConnection connection) {
		if (connection.getAdditionalDetails() != null) {
			HashMap<String, Object> additionalDetails = mapper.convertValue(connection.getAdditionalDetails(),
					HashMap.class);
			if (additionalDetails.getOrDefault(WSCalculationConstant.ADHOC_PENALTY, null) != null) {
				estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_ADHOC_PENALTY)
						.estimateAmount(
								new BigDecimal(additionalDetails.get(WSCalculationConstant.ADHOC_PENALTY).toString()))
						.build());
			}
			if (additionalDetails.getOrDefault(WSCalculationConstant.ADHOC_REBATE, null) != null) {
				estimates
						.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_ADHOC_REBATE)
								.estimateAmount(new BigDecimal(
										additionalDetails.get(WSCalculationConstant.ADHOC_REBATE).toString()).negate())
								.build());
			}
		}
	}
	
	public static void main(String[] args) {
		Date d = new Date();	
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("IST"));		
		cal.setTime(d);	
		setTimeToEndofDay(cal);		
		EstimationService service = new EstimationService();
		HashMap<String, Object> billingPeriod = new HashMap<String, Object>();
		service.getHalfYearStartAndEndDate(billingPeriod);		
		//EstimationService.enclosing_method(){startingDay=1614537000000, endingDay=1619807399999}
		 
		
	}
	
}

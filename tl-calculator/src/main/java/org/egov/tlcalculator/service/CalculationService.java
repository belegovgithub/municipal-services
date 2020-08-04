package org.egov.tlcalculator.service;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tlcalculator.config.TLCalculatorConfigs;
import org.egov.tlcalculator.kafka.broker.TLCalculatorProducer;
import org.egov.tlcalculator.repository.builder.BillingslabQueryBuilder;
import org.egov.tlcalculator.repository.BillingslabRepository;
import org.egov.tlcalculator.repository.ServiceRequestRepository;
import org.egov.tlcalculator.utils.CalculationUtils;
import org.egov.tlcalculator.utils.TLCalculatorConstants;
import org.egov.tlcalculator.web.models.*;
import org.egov.tlcalculator.web.models.enums.CalculationType;
import org.egov.tlcalculator.web.models.FeeAndBillingSlabIds;
import org.egov.tlcalculator.web.models.tradelicense.TradeLicense;
import org.egov.tlcalculator.web.models.tradelicense.TradeLicense.StatusEnum;
import org.egov.tlcalculator.web.models.demand.Category;
import org.egov.tlcalculator.web.models.demand.TaxHeadEstimate;
import org.egov.tlcalculator.web.models.tradelicense.TradeUnit;
import org.egov.tlcalculator.web.models.tradelicense.EstimatesAndSlabs;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.JsonPath;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.egov.tlcalculator.utils.TLCalculatorConstants.businessService_TL;
import org.egov.tlcalculator.web.models.tradelicense.TradeLicense;

@Service
@Slf4j
public class CalculationService {


    @Autowired
    private BillingslabRepository repository;

    @Autowired
    private BillingslabQueryBuilder queryBuilder;

    @Autowired
    private TLCalculatorConfigs config;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private CalculationUtils utils;

    @Autowired
    private DemandService demandService;

    @Autowired
    private TLCalculatorProducer producer;

    @Autowired
    private MDMSService mdmsService;

    @Autowired
    private TLRenewalCalculation tlRenewal;

    /**
     * Calculates tax estimates and creates demand 
     * @param calculationReq The calculationCriteria request
     * @return List of calculations for all applicationNumbers or tradeLicenses in calculationReq
     * Changes : In the application status is INITIATED or PENDINGAPPLFEE then it demand will be generated only for application fee otherwise it will be for TL tax ( license fee )
     */
   public List<Calculation> calculate(CalculationReq calculationReq){
	    String tenantId = calculationReq.getCalulationCriteria().get(0).getTenantId();
       StatusEnum status = calculationReq.getCalulationCriteria().get(0).getTradelicense().getStatus();
       Object mdmsData = mdmsService.mDMSCall(calculationReq.getRequestInfo(),tenantId);
       
       List<Calculation> calculations = getCalculation(calculationReq.getRequestInfo(),
               calculationReq.getCalulationCriteria(),mdmsData);
       
       if(status==TradeLicense.StatusEnum.INITIATED  || status==TradeLicense.StatusEnum.PENDINGAPPLFEE) {
    	   System.out.println("initiate status");
    	   for (Calculation calculation : calculations) {
    		   List<TaxHeadEstimate> taxheadEsts =  calculation.getTaxHeadEstimates().stream().filter(
        			   taxheadEst -> (taxheadEst.getTaxHeadCode().equals(config.getAppFeeTaxHead())) && !taxheadEst.getEstimateAmount().equals(BigDecimal.ZERO)).collect(Collectors.toList());
    		   if(taxheadEsts.size()==0) {
    			   taxheadEsts = calculation.getTaxHeadEstimates().stream().filter(
            			   taxheadEst -> !(taxheadEst.getTaxHeadCode().equals(config.getAppFeeTaxHead()))) .collect(Collectors.toList());
    		   }
    		   calculation.setTaxHeadEstimates(taxheadEsts);
		}
       }
       else {
    	   for (Calculation calculation : calculations) {
    	   List<TaxHeadEstimate> taxheadEsts = calculation.getTaxHeadEstimates().stream().filter(
    			   taxheadEst -> !(taxheadEst.getTaxHeadCode().equals(config.getAppFeeTaxHead()))) .collect(Collectors.toList());
    	   calculation.setTaxHeadEstimates(taxheadEsts);
    	   }
       }
      
       demandService.generateDemand(calculationReq.getRequestInfo(),calculations,mdmsData,businessService_TL);
       CalculationRes calculationRes = CalculationRes.builder().calculations(calculations).build();
       producer.push(config.getSaveTopic(),calculationRes);
       return calculations;
   }

   
   
    /***
     * Calculates tax estimates
     * @param requestInfo The requestInfo of the calculation request
     * @param criterias list of CalculationCriteria containing the tradeLicense or applicationNumber
     * @return  List of calculations for all applicationNumbers or tradeLicenses in criterias
     */
  public List<Calculation> getCalculation(RequestInfo requestInfo, List<CalulationCriteria> criterias,Object mdmsData){
      List<Calculation> calculations = new LinkedList<>();
      for(CalulationCriteria criteria : criterias) {
          TradeLicense license;
          if (criteria.getTradelicense()==null && criteria.getApplicationNumber() != null) {
              license = utils.getTradeLicense(requestInfo, criteria.getApplicationNumber(), criteria.getTenantId());
             
              criteria.setTradelicense(license);
          } 
          
          EstimatesAndSlabs estimatesAndSlabs = getTaxHeadEstimates(criteria,requestInfo,mdmsData);
          List<TaxHeadEstimate> taxHeadEstimates = estimatesAndSlabs.getEstimates();
          FeeAndBillingSlabIds tradeTypeFeeAndBillingSlabIds = estimatesAndSlabs.getTradeTypeFeeAndBillingSlabIds();
          FeeAndBillingSlabIds accessoryFeeAndBillingSlabIds = null;
          if(estimatesAndSlabs.getAccessoryFeeAndBillingSlabIds()!=null)
              accessoryFeeAndBillingSlabIds = estimatesAndSlabs.getAccessoryFeeAndBillingSlabIds();
          Calculation calculation = new Calculation();
          calculation.setTradeLicense(criteria.getTradelicense());
          calculation.setTenantId(criteria.getTenantId());
          calculation.setTaxHeadEstimates(taxHeadEstimates);
          calculation.setTradeTypeBillingIds(tradeTypeFeeAndBillingSlabIds);
          if(accessoryFeeAndBillingSlabIds!=null)
              calculation.setAccessoryBillingIds(accessoryFeeAndBillingSlabIds);

          calculations.add(calculation);

      }
      return calculations;
  }


    /**
     * Creates TacHeadEstimates
     * @param calulationCriteria CalculationCriteria containing the tradeLicense or applicationNumber
     * @param requestInfo The requestInfo of the calculation request
     * @return TaxHeadEstimates and the billingSlabs used to calculate it
     */
    private EstimatesAndSlabs getTaxHeadEstimates(CalulationCriteria calulationCriteria, RequestInfo requestInfo,Object mdmsData ){
      List<TaxHeadEstimate> estimates = new LinkedList<>();
      EstimatesAndSlabs  estimatesAndSlabs = getBaseTax(calulationCriteria,requestInfo,mdmsData);

      estimates.addAll(estimatesAndSlabs.getEstimates());
      if(calulationCriteria.getTradelicense().getTradeLicenseDetail().getAdhocPenalty()!=null)
          estimates.add(getAdhocPenalty(calulationCriteria));

      if(calulationCriteria.getTradelicense().getTradeLicenseDetail().getAdhocExemption()!=null)
          estimates.add(getAdhocExemption(calulationCriteria));
      

      Object additionalData = calulationCriteria.getTradelicense().getTradeLicenseDetail().getAdditionalDetail();
      if(additionalData!=null) {
      String garbageCharges =  JsonPath.read(additionalData, "$.garbageCharges");
     if(garbageCharges!=null) {
    	 estimates.add(getGarbageCharges(calulationCriteria));
     	}
      }
      
      estimatesAndSlabs.setEstimates(estimates);

      return estimatesAndSlabs;
  }


    /**
     * Calculates base tax and cretaes its taxHeadEstimate
     * @param calulationCriteria CalculationCriteria containing the tradeLicense or applicationNumber
     * @param requestInfo The requestInfo of the calculation request
     * @return BaseTax taxHeadEstimate and billingSlabs used to calculate it
     */
  private EstimatesAndSlabs getBaseTax(CalulationCriteria calulationCriteria, RequestInfo requestInfo,Object mdmsData){
      TradeLicense license = calulationCriteria.getTradelicense();
      EstimatesAndSlabs estimatesAndSlabs = new EstimatesAndSlabs();
      BillingSlabSearchCriteria searchCriteria = new BillingSlabSearchCriteria();
      searchCriteria.setTenantId(license.getTenantId());
      searchCriteria.setStructureType(license.getTradeLicenseDetail().getStructureType());
      searchCriteria.setLicenseType(license.getLicenseType().toString());


      Map calculationTypeMap = mdmsService.getCalculationType(requestInfo,license,mdmsData);
      String tradeUnitCalculationType = (String)calculationTypeMap.get(TLCalculatorConstants.MDMS_CALCULATIONTYPE_TRADETYPE);
      String accessoryCalculationType  = (String)calculationTypeMap.get(TLCalculatorConstants.MDMS_CALCULATIONTYPE_ACCESSORY);

      FeeAndBillingSlabIds tradeTypeFeeAndBillingSlabIds = getTradeUnitFeeAndBillingSlabIds(license,CalculationType
              .fromValue(tradeUnitCalculationType));
      BigDecimal tradeUnitFee = tradeTypeFeeAndBillingSlabIds.getFee();
      BigDecimal tradeAppFee = tradeTypeFeeAndBillingSlabIds.getApplicationFee();

      estimatesAndSlabs.setTradeTypeFeeAndBillingSlabIds(tradeTypeFeeAndBillingSlabIds);
      BigDecimal accessoryFee = new BigDecimal(0);

      if(!CollectionUtils.isEmpty(license.getTradeLicenseDetail().getAccessories())){
           FeeAndBillingSlabIds accessoryFeeAndBillingSlabIds = getAccessoryFeeAndBillingSlabIds(license,CalculationType
                   .fromValue(accessoryCalculationType));
           accessoryFee = accessoryFeeAndBillingSlabIds.getFee();
           estimatesAndSlabs.setAccessoryFeeAndBillingSlabIds(accessoryFeeAndBillingSlabIds);
      }

      TaxHeadEstimate estimate = new TaxHeadEstimate();
      List<TaxHeadEstimate> estimateList = new ArrayList<>();
      BigDecimal totalTax = tradeUnitFee.add(accessoryFee);

      if(totalTax.compareTo(BigDecimal.ZERO)==-1)
          throw new CustomException("INVALID AMOUNT","Tax amount is negative");

      estimate.setEstimateAmount(totalTax);
      estimate.setCategory(Category.TAX);
      if(license.getApplicationType() != null && license.getApplicationType().toString().equals(TLCalculatorConstants.APPLICATION_TYPE_RENEWAL)){
          estimate.setTaxHeadCode(config.getRenewTaxHead());
          estimateList.add(estimate);
          estimateList.addAll(tlRenewal.tlRenewalCalculation(requestInfo,calulationCriteria,mdmsData,totalTax));
      }else{
          estimate.setTaxHeadCode(config.getBaseTaxHead());
          estimateList.add(estimate);
          TaxHeadEstimate estimateAppfee = new TaxHeadEstimate();
          estimateAppfee.setEstimateAmount(tradeAppFee);
          estimateAppfee.setCategory(Category.FEE);
          estimateAppfee.setTaxHeadCode(config.getAppFeeTaxHead());
          estimateList.add(estimateAppfee);
      }
    
      estimatesAndSlabs.setEstimates(estimateList);
      return estimatesAndSlabs;
  }


    /**
     *  Creates taxHeadEstimates for AdhocPenalty
     * @param calulationCriteria CalculationCriteria containing the tradeLicense or applicationNumber
     * @return AdhocPenalty taxHeadEstimates
     */
  private TaxHeadEstimate getAdhocPenalty(CalulationCriteria calulationCriteria){
      TradeLicense license = calulationCriteria.getTradelicense();
      TaxHeadEstimate estimate = new TaxHeadEstimate();
      estimate.setEstimateAmount(license.getTradeLicenseDetail().getAdhocPenalty());
      estimate.setTaxHeadCode(config.getAdhocPenaltyTaxHead());
      estimate.setCategory(Category.PENALTY);
      return estimate;
  }

 

    /**
     *  Creates taxHeadEstimates for AdhocRebate
     * @param calulationCriteria CalculationCriteria containing the tradeLicense or applicationNumber
     * @return AdhocRebate taxHeadEstimates
     */
    private TaxHeadEstimate getAdhocExemption(CalulationCriteria calulationCriteria){
        TradeLicense license = calulationCriteria.getTradelicense();
        TaxHeadEstimate estimate = new TaxHeadEstimate();
        estimate.setEstimateAmount(license.getTradeLicenseDetail().getAdhocExemption());
        estimate.setTaxHeadCode(config.getAdhocExemptionTaxHead());
        estimate.setCategory(Category.EXEMPTION);
        return estimate;
    }
    
    
    /**
     *  Creates taxHeadEstimates for Garbage Charges
     * @param calulationCriteria CalculationCriteria containing the tradeLicense or applicationNumber
     * @return Garbage Charges taxHeadEstimates
     */
    private TaxHeadEstimate getGarbageCharges(CalulationCriteria calulationCriteria){
        TaxHeadEstimate estimate = new TaxHeadEstimate();
        Object additionalData = calulationCriteria.getTradelicense().getTradeLicenseDetail().getAdditionalDetail();
        String garbageCharges =  JsonPath.read(additionalData, "$.garbageCharges");
        estimate.setEstimateAmount(new BigDecimal(garbageCharges));
        estimate.setTaxHeadCode(config.getGarbageChargesTaxHead());
        estimate.setCategory(Category.FEE);
        return estimate;
    }



    /**
     * @param license TradeLicense for which fee has to be calculated
     * @param calculationType Calculation logic to be used
     * @return TradeUnit Fee and billingSlab used to calculate it
     * Changes : Added Applicatino fee taxhead
     */
  private FeeAndBillingSlabIds getTradeUnitFeeAndBillingSlabIds(TradeLicense license, CalculationType calculationType ){

      List<BigDecimal> tradeUnitFees = new LinkedList<>();
      List<BigDecimal> tradeAppFees = new LinkedList<>();
      BigDecimal tradeAppTotalFee = null ;
      List<TradeUnit> tradeUnits = license.getTradeLicenseDetail().getTradeUnits();
      List<String> billingSlabIds = new LinkedList<>();
      int i = 0;
       for(TradeUnit tradeUnit : tradeUnits)
       { if(tradeUnit.getActive())
         {
              List<Object> preparedStmtList = new ArrayList<>();
              BillingSlabSearchCriteria searchCriteria = new BillingSlabSearchCriteria();
              searchCriteria.setTenantId(license.getTenantId());
              searchCriteria.setStructureType(license.getTradeLicenseDetail().getStructureType());
              searchCriteria.setLicenseType(license.getLicenseType().toString());
              searchCriteria.setTradeType(tradeUnit.getTradeType());
              if(tradeUnit.getUomValue()!=null)
              {
                  searchCriteria.setUomValue(Double.parseDouble(tradeUnit.getUomValue()));
                  searchCriteria.setUom(tradeUnit.getUom());
              }
              // Call the Search
              String query = queryBuilder.getSearchQuery(searchCriteria, preparedStmtList);
              log.info("query "+query);
              log.info("preparedStmtList "+preparedStmtList.toString());
              List<BillingSlab> billingSlabs = repository.getDataFromDB(query, preparedStmtList);
              
              if(billingSlabs.size()>1)
                  throw new CustomException("BILLINGSLAB ERROR","Found multiple BillingSlabs for the given TradeType");
              if(CollectionUtils.isEmpty(billingSlabs))
                  throw new CustomException("BILLINGSLAB ERROR","No BillingSlab Found for the given tradeType");
             System.out.println("TradeUnit: "+tradeUnit.getTradeType()+ " rate: "+billingSlabs.get(0).getRate());

             billingSlabIds.add(billingSlabs.get(0).getId()+"|"+i+"|"+tradeUnit.getId());

            	 
	             if(billingSlabs.get(0).getType().equals(BillingSlab.TypeEnum.FLAT))
	                 tradeUnitFees.add(billingSlabs.get(0).getRate());
	             	 
	        //         tradeUnitTotalFee = tradeUnitTotalFee.add(billingSlabs.get(0).getRate());
	
	             if(billingSlabs.get(0).getType().equals(BillingSlab.TypeEnum.RATE)){
	                 BigDecimal uomVal = new BigDecimal(tradeUnit.getUomValue());
	                 tradeUnitFees.add(billingSlabs.get(0).getRate().multiply(uomVal));
	                 //tradeUnitTotalFee = tradeUnitTotalFee.add(billingSlabs.get(0).getRate().multiply(uomVal));
	             }
	             tradeAppFees.add(billingSlabs.get(0).getApplicationFee());
	             tradeAppTotalFee = billingSlabs.get(0).getApplicationFee();
           i++;
         }
      }
       
      BigDecimal tradeUnitTotalFee = getTotalFee(tradeUnitFees,calculationType);
     // BigDecimal tradeAppTotalFee = getTotalFee(tradeAppFees,calculationType);

      FeeAndBillingSlabIds feeAndBillingSlabIds = new FeeAndBillingSlabIds();
      feeAndBillingSlabIds.setFee(tradeUnitTotalFee);
      feeAndBillingSlabIds.setApplicationFee(tradeAppTotalFee);
      feeAndBillingSlabIds.setBillingSlabIds(billingSlabIds);
      feeAndBillingSlabIds.setId(UUID.randomUUID().toString());

      return feeAndBillingSlabIds;
  }


    /**
     * @param license TradeLicense for which fee has to be calculated
     * @param calculationType Calculation logic to be used
     * @return Accessory Fee and billingSlab used to calculate it
     */
  private FeeAndBillingSlabIds getAccessoryFeeAndBillingSlabIds(TradeLicense license, CalculationType calculationType){

      List<BigDecimal> accessoryFees = new LinkedList<>();
      List<String> billingSlabIds = new LinkedList<>();

      List<Accessory> accessories = license.getTradeLicenseDetail().getAccessories();
      int i = 0;
       for(Accessory accessory : accessories)
       { if(accessory.getActive())
         {
               List<Object> preparedStmtList = new ArrayList<>();
               BillingSlabSearchCriteria searchCriteria = new BillingSlabSearchCriteria();
               searchCriteria.setTenantId(license.getTenantId());
               searchCriteria.setAccessoryCategory(accessory.getAccessoryCategory());
              if(accessory.getUomValue()!=null)
              {
                  searchCriteria.setUomValue(Double.parseDouble(accessory.getUomValue()));
                  searchCriteria.setUom(accessory.getUom());
              }
              // Call the Search
              String query = queryBuilder.getSearchQuery(searchCriteria, preparedStmtList);
              List<BillingSlab> billingSlabs = repository.getDataFromDB(query, preparedStmtList);

              if(billingSlabs.size()>1)
                  throw new CustomException("BILLINGSLAB ERROR","Found multiple BillingSlabs for the given accessories ");
              if(CollectionUtils.isEmpty(billingSlabs))
                  throw new CustomException("BILLINGSLAB ERROR","No BillingSlab Found for the given accessory");
             System.out.println("Accessory: "+accessory.getAccessoryCategory()+ " rate: "+billingSlabs.get(0).getRate());
             billingSlabIds.add(billingSlabs.get(0).getId()+"|"+i+"|"+accessory.getId());
             if(billingSlabs.get(0).getType().equals(BillingSlab.TypeEnum.FLAT)){
                 BigDecimal count = accessory.getCount()==null ? BigDecimal.ONE : new BigDecimal(accessory.getCount());
                 accessoryFees.add(billingSlabs.get(0).getRate().multiply(count));
             }
            //     accessoryTotalFee = accessoryTotalFee.add(billingSlabs.get(0).getRate());

             if(billingSlabs.get(0).getType().equals(BillingSlab.TypeEnum.RATE)){
                 BigDecimal uomVal = new BigDecimal(accessory.getUomValue());
                 accessoryFees.add(billingSlabs.get(0).getRate().multiply(uomVal));
              //   accessoryTotalFee = accessoryTotalFee.add(billingSlabs.get(0).getRate().multiply(uomVal));
             }
             i++;
         }
      }

      BigDecimal accessoryTotalFee = getTotalFee(accessoryFees,calculationType);
      FeeAndBillingSlabIds feeAndBillingSlabIds = new FeeAndBillingSlabIds();
      feeAndBillingSlabIds.setFee(accessoryTotalFee);
      feeAndBillingSlabIds.setBillingSlabIds(billingSlabIds);
      feeAndBillingSlabIds.setId(UUID.randomUUID().toString());


      return feeAndBillingSlabIds;
  }


    /**
     * Calculates total fee of by applying logic on list based on calculationType
     * @param fees List of fee for different tradeType or accessories
     * @param calculationType Calculation logic to be used
     * @return Total Fee
     */
  private BigDecimal getTotalFee(List<BigDecimal> fees,CalculationType calculationType){
      BigDecimal totalFee = BigDecimal.ZERO;
      //Summation
      if(calculationType.equals(CalculationType.SUM))
          totalFee = fees.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

      //Average
      if(calculationType.equals(CalculationType.AVERAGE))
          totalFee = (fees.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                  .divide(new BigDecimal(fees.size()))).setScale(2,2);

      //Max
      if(calculationType.equals(CalculationType.MAX))
          totalFee = fees.stream().reduce(BigDecimal::max).get();

      //Min
      if(calculationType.equals(CalculationType.MIN))
          totalFee = fees.stream().reduce(BigDecimal::min).get();

       return totalFee;
  }









}

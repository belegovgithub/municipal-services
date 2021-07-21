package org.egov.wscalculation.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.egov.wscalculation.constants.WSCalculationConstant;
import org.egov.wscalculation.web.models.TaxHeadEstimate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Service
public class PayService {

	@Autowired
	private MasterDataService mDService;
	
	@Autowired
	private EstimationService estimationService;
	
	
	@Value("${app.timezone}")
	private String timeZone;
	
	@Autowired
	private ObjectMapper mapper;

	/**
	 * Decimal is ceiled for all the tax heads
	 * 
	 * if the decimal is greater than 0.5 upper bound will be applied
	 * 
	 * else if decimal is lesser than 0.5 lower bound is applied
	 * 
	 */
	public TaxHeadEstimate roundOfDecimals(BigDecimal creditAmount, BigDecimal debitAmount, boolean isConnectionFee) {
		BigDecimal roundOffPos = BigDecimal.ZERO;
		BigDecimal roundOffNeg = BigDecimal.ZERO;
        String taxHead = isConnectionFee ? WSCalculationConstant.WS_Round_Off : WSCalculationConstant.WS_ONE_TIME_FEE_ROUND_OFF;
		BigDecimal result = creditAmount.add(debitAmount);
		BigDecimal roundOffAmount = result.setScale(2, 2);
		BigDecimal reminder = roundOffAmount.remainder(BigDecimal.ONE);

		if (reminder.doubleValue() >= 0.5)
			roundOffPos = roundOffPos.add(BigDecimal.ONE.subtract(reminder));
		else if (reminder.doubleValue() < 0.5)
			roundOffNeg = roundOffNeg.add(reminder).negate();

		if (roundOffPos.doubleValue() > 0)
			return TaxHeadEstimate.builder().estimateAmount(roundOffPos).taxHeadCode(taxHead)
					.build();
		else if (roundOffNeg.doubleValue() < 0)
			return TaxHeadEstimate.builder().estimateAmount(roundOffNeg).taxHeadCode(taxHead)
					.build();
		else
			return null;
	}
	
	/**
	 * 
	 * @param waterCharge - Water Charge Amount
	 * @param assessmentYear - Assessment Year
	 * @param timeBasedExemptionMasterMap - Time Based Exemption Master Data
	 * @param billingExpiryDate - Billing Expiry Date
	 * @return estimation of time based exemption
	 */
	
	public Map<String, BigDecimal> applyPenaltyRebateAndInterest(BigDecimal waterCharge,
			String assessmentYear, Map<String, JSONArray> timeBasedExemptionMasterMap, Long billingExpiryDate) {

		System.out.println("Came for pen/inter");
		if (BigDecimal.ZERO.compareTo(waterCharge) >= 0)
			return Collections.emptyMap();
		Map<String, BigDecimal> estimates = new HashMap<>();
		
		//long currentUTC = System.currentTimeMillis();
		
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
		long currentUTC = calendar.getTimeInMillis();
		long numberOfDaysInMillis = billingExpiryDate - currentUTC;
		
		BigDecimal noOfDays = BigDecimal.valueOf((TimeUnit.MILLISECONDS.toDays(Math.abs(numberOfDaysInMillis))));
		System.out.println("NO of chargeable days="+noOfDays);
		if(BigDecimal.ONE.compareTo(noOfDays) <= 0) noOfDays = noOfDays.add(BigDecimal.ONE);
		System.out.println("Penality=="+timeBasedExemptionMasterMap.get(WSCalculationConstant.WC_PENANLTY_MASTER));
		BigDecimal penalty = getApplicablePenalty(waterCharge, noOfDays, timeBasedExemptionMasterMap.get(WSCalculationConstant.WC_PENANLTY_MASTER));
		BigDecimal interest = getApplicableInterest(waterCharge, noOfDays, timeBasedExemptionMasterMap.get(WSCalculationConstant.WC_INTEREST_MASTER));
		estimates.put(WSCalculationConstant.WS_TIME_PENALTY, penalty.setScale(2, 2));
		estimates.put(WSCalculationConstant.WS_TIME_INTEREST, interest.setScale(2, 2));
		return estimates;
	}
	
	//BEL implementation for CBs
	public Map<String, BigDecimal> calculatePenaltyForWaterBill(BigDecimal waterCharge, Map<String, JSONArray> timeBasedExemptionMasterMap) {

		if (BigDecimal.ZERO.compareTo(waterCharge) >= 0)
			return Collections.emptyMap();
		Map<String, BigDecimal> estimates = new HashMap<>();
		BigDecimal applicablePenalty = BigDecimal.ZERO;
	
		JSONArray penaltyMaster = timeBasedExemptionMasterMap.get(WSCalculationConstant.WC_PENANLTY_MASTER);
		if (null == penaltyMaster) {
			estimates.put(WSCalculationConstant.WS_TIME_PENALTY, applicablePenalty.setScale(2, 2));
			//return estimates;
		}
		else {
			JSONObject penaltyJsonObj = mapper.convertValue(penaltyMaster.get(0), JSONObject.class);
			BigDecimal rate = null != penaltyJsonObj.get(WSCalculationConstant.RATE_FIELD_NAME)
					? BigDecimal.valueOf(((Number) penaltyJsonObj.get(WSCalculationConstant.RATE_FIELD_NAME)).doubleValue())
					: null;
			
			if (rate != null)
				applicablePenalty = waterCharge.multiply(rate.divide(WSCalculationConstant.HUNDRED));
					
		}	
		estimates.put(WSCalculationConstant.WS_TIME_PENALTY, applicablePenalty.setScale(2, 2));
		return estimates;
	}
	
	
	
	public Map<String, BigDecimal> applyPenaltyRebateAndInterest_old(BigDecimal waterCharge,
			String assessmentYear, Map<String, JSONArray> timeBasedExemptionMasterMap, Long billingExpiryDate) {

		if (BigDecimal.ZERO.compareTo(waterCharge) >= 0)
			return Collections.emptyMap();
		Map<String, BigDecimal> estimates = new HashMap<>();
		long currentUTC = System.currentTimeMillis();
		long numberOfDaysInMillis = billingExpiryDate - currentUTC;	
		
		BigDecimal noOfDays = BigDecimal.valueOf((TimeUnit.MILLISECONDS.toDays(Math.abs(numberOfDaysInMillis))));
		if(BigDecimal.ONE.compareTo(noOfDays) <= 0) noOfDays = noOfDays.add(BigDecimal.ONE);
		BigDecimal penalty = getApplicablePenalty(waterCharge, noOfDays, timeBasedExemptionMasterMap.get(WSCalculationConstant.WC_PENANLTY_MASTER));
		BigDecimal interest = getApplicableInterest(waterCharge, noOfDays, timeBasedExemptionMasterMap.get(WSCalculationConstant.WC_INTEREST_MASTER));
		estimates.put(WSCalculationConstant.WS_TIME_PENALTY, penalty.setScale(2, 2));
		estimates.put(WSCalculationConstant.WS_TIME_INTEREST, interest.setScale(2, 2));
		return estimates;
	}

	/**
	 * Returns the Amount of penalty that has to be applied on the given tax amount for the given period
	 * 
	 * @param taxAmt - Tax Amount
	 * @param assessmentYear - Assessment Year
	 * @return applicable penalty for given time
	 */
	public BigDecimal getPenalty(BigDecimal taxAmt, String assessmentYear, JSONArray penaltyMasterList, BigDecimal noOfDays) {

		BigDecimal penaltyAmt = BigDecimal.ZERO;
		Map<String, Object> penalty = mDService.getApplicableMaster(assessmentYear, penaltyMasterList);
		if (null == penalty) return penaltyAmt;
			penaltyAmt = mDService.calculateApplicable(taxAmt, penalty);
		return penaltyAmt;
	}
	
	/**
	 * 
	 * @param waterCharge - Water Charge amount
	 * @param noOfDays - No.Of.Days
	 * @param config
	 *            master configuration
	 * @return applicable penalty
	 */
	public BigDecimal getApplicablePenalty(BigDecimal waterCharge, BigDecimal noOfDays, JSONArray config) {
		BigDecimal applicablePenalty = BigDecimal.ZERO;
		
		Map<String, Object> penaltyMaster = mDService.getApplicableMaster(estimationService.getAssessmentYear(), config);
		
		if (null == penaltyMaster) return applicablePenalty;
		BigDecimal daysApplicable = null != penaltyMaster.get(WSCalculationConstant.DAYA_APPLICABLE_NAME)
				? BigDecimal.valueOf(((Number) penaltyMaster.get(WSCalculationConstant.DAYA_APPLICABLE_NAME)).intValue())
				: null;
		if (daysApplicable == null)
			return applicablePenalty;
		BigDecimal daysDiff = noOfDays.subtract(daysApplicable);
		if (daysDiff.compareTo(BigDecimal.ONE) < 0) {
			return applicablePenalty;
		}
		BigDecimal rate = null != penaltyMaster.get(WSCalculationConstant.RATE_FIELD_NAME)
				? BigDecimal.valueOf(((Number) penaltyMaster.get(WSCalculationConstant.RATE_FIELD_NAME)).doubleValue())
				: null;

		BigDecimal flatAmt = null != penaltyMaster.get(WSCalculationConstant.FLAT_AMOUNT_FIELD_NAME)
				? BigDecimal
						.valueOf(((Number) penaltyMaster.get(WSCalculationConstant.FLAT_AMOUNT_FIELD_NAME)).doubleValue())
				: BigDecimal.ZERO;

		if (rate == null)
			applicablePenalty = flatAmt.compareTo(waterCharge) > 0 ? BigDecimal.ZERO : flatAmt;
		else {
			// rate of penalty
			System.out.println("apply penality/regate rate="+rate+"watercharge="+waterCharge);
			applicablePenalty = waterCharge.multiply(rate.divide(WSCalculationConstant.HUNDRED));
		}
		
		BigDecimal minAmount = null != penaltyMaster.get(WSCalculationConstant.MIN_AMOUNT_FIELD_NAME)
				? BigDecimal.valueOf(((Number) penaltyMaster.get(WSCalculationConstant.MIN_AMOUNT_FIELD_NAME)).doubleValue())
				: null;
	    if(minAmount!=null) {
	    	if (minAmount.compareTo(applicablePenalty) > 0 ) {
	    		applicablePenalty = minAmount;
	    	}
	    }
		
		return applicablePenalty;
	}
	
	/**
	 * 
	 * @param waterCharge - Water Charge
	 * @param noOfDays - No.Of Days value
	 * @param config
	 *            master configuration
	 * @return applicable Interest
	 */
	public BigDecimal getApplicableInterest(BigDecimal waterCharge, BigDecimal noOfDays, JSONArray config) {
		BigDecimal applicableInterest = BigDecimal.ZERO;
		Map<String, Object> interestMaster = mDService.getApplicableMaster(estimationService.getAssessmentYear(), config);
		if (null == interestMaster) return applicableInterest;
		BigDecimal daysApplicable = null != interestMaster.get(WSCalculationConstant.DAYA_APPLICABLE_NAME)
				? BigDecimal.valueOf(((Number) interestMaster.get(WSCalculationConstant.DAYA_APPLICABLE_NAME)).intValue())
				: null;
		if (daysApplicable == null)
			return applicableInterest;
		BigDecimal daysDiff = noOfDays.subtract(daysApplicable);
		if (daysDiff.compareTo(BigDecimal.ONE) < 0) {
			return applicableInterest;
		}
		BigDecimal rate = null != interestMaster.get(WSCalculationConstant.RATE_FIELD_NAME)
				? BigDecimal.valueOf(((Number) interestMaster.get(WSCalculationConstant.RATE_FIELD_NAME)).doubleValue())
				: null;

		BigDecimal flatAmt = null != interestMaster.get(WSCalculationConstant.FLAT_AMOUNT_FIELD_NAME)
				? BigDecimal
						.valueOf(((Number) interestMaster.get(WSCalculationConstant.FLAT_AMOUNT_FIELD_NAME)).doubleValue())
				: BigDecimal.ZERO;

		if (rate == null)
			applicableInterest = flatAmt.compareTo(waterCharge) > 0 ? BigDecimal.ZERO : flatAmt;
		else{
			// rate of interest
			applicableInterest = waterCharge.multiply(rate.divide(WSCalculationConstant.HUNDRED));
		}
		//applicableInterest.multiply(noOfDays.divide(BigDecimal.valueOf(365), 6, 5));
		return applicableInterest;
	}
}

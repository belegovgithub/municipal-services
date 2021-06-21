package org.egov.wscalculation.web.models;

import java.math.BigDecimal;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;



@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class BillEstimation {
	
	 @JsonProperty("BillingSlab")
	 private BillingSlab billingSlab;
	
	  
	 @JsonProperty("billAmount")
	 double billAmount;
	 
	 @JsonProperty("motorChargePayable")
	 double motorChargePayable;
	 
	 @JsonProperty("payableBillAmount")
	 double payableBillAmount;
	 
	 @JsonProperty("billingCycleEndDate")
	 long billingCycleEndDate;
	 
	 @JsonProperty("monthsToCharge")
	 double monthsToCharge;
	 

}
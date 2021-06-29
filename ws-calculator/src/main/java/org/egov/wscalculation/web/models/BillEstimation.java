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
	 BigDecimal waterCharge;
	 
	 @JsonProperty("motorChargePayable")
	 double motorChargePayable;
	 
	 @JsonProperty("payableBillAmount")
	 double payableBillAmount;
	 
	 @JsonProperty("billingDate")
	 private Long billingDate;
	 
	 @JsonProperty("billingCycleEndDate")
	 Long billingCycleEndDate;
	 
	 @JsonProperty("monthsToCharge")
	 double monthsToCharge;
	 
	 private String calculationAttribute;
	 
	 private Double  totalUOM; 
	 
	 private BigDecimal maintenanceCharge;
	 

}
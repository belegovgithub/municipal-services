package org.egov.pt.excelimport;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import org.egov.pt.models.AuditDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A object holds a demand and collection values for a tax head and period.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExcelColumns   {
	
        private String propertyId;

        private String taxHeadMasterCode;

        private BigDecimal taxAmount;
        
        @Default
        private BigDecimal collectedAmount = BigDecimal.ZERO;

}
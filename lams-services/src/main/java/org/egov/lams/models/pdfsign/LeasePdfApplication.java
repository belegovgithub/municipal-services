package org.egov.lams.models.pdfsign;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeasePdfApplication {

	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("name")
	private String name;

	@JsonProperty("mobileNo")
	private String mobileNo;
	
	@JsonProperty("surveyNo")
	private String surveyNo;
	
	@JsonProperty("fatherOrHusbandName")
    private String fatherOrHusbandName;

	@JsonProperty("forEsign")
	private boolean forEsign;
	
	@JsonProperty("aadhaarNumber")
	private String aadhaarNumber;

	@JsonProperty("months")
	private String months;
	
}

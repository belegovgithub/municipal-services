package org.egov.lams.models.pdfsign;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
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
	
	@JsonProperty("surveyId")
	private String surveyId;
	
	@JsonProperty("area")
	private String area;
	
	@JsonProperty("addressee")
	private String addressee;
	
	@JsonProperty("instruction")
	private String instruction;
	
	@JsonProperty("instruction1")
	private String instruction1;
	
	@JsonProperty("instruction2")
	private String instruction2;
	
}

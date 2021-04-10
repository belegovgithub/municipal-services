package org.bel.birthdeath.birth.model;

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
public class SearchCriteria {

	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("dateOfBirth")
	private String dateOfBirth;
	
	@JsonProperty("motherName")
	private String motherName;
	
	@JsonProperty("registrationNo")
	private String registrationNo;
	
	@JsonProperty("gender")
	private Integer gender;
	
	@JsonProperty("hospitalId")
	private String hospitalId;
	
	@JsonProperty("birthDtlId")
	private String birthDtlId;
	
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("consumerCode")
	private String consumerCode;
	
	@JsonProperty("fatherName")
	private String fatherName;
	
	private String birthcertificateno; 
	
	private String fromDate;
	
	private String toDate;
	
	private String token;
}

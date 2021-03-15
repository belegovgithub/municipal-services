package org.bel.birthdeath.death.model;

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

	@JsonProperty("dateOfDeath")
	private String dateOfDeath;
	
	@JsonProperty("motherName")
	private String motherName;
	
	@JsonProperty("registrationNo")
	private String registrationNo;
	
	@JsonProperty("gender")
	private Integer gender;
	
	@JsonProperty("hospitalId")
	private String hospitalId;
	
	@JsonProperty("deathDtlId")
	private String deathDtlId;
	
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("consumerCode")
	private String consumerCode;
	
	@JsonProperty("fatherName")
	private String fatherName;
	
	@JsonProperty("name")
	private String name;
	
	@JsonProperty("spouseName")
	private String spouseName;
	
	private String deathcertificateno;
}

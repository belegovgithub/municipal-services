package org.bel.birthdeath.birth.model;

import java.util.List;
import java.util.Set;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
	
	@JsonProperty("hospitalName")
	private String hospitalName;
	
	@JsonProperty("birthDtlId")
	private String birthDtlId;
	
	@JsonProperty("id")
	private String id;
	
}

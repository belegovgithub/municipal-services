package org.egov.pt.models;

import java.util.Set;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.models.enums.Status;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertySearchCriteria {

	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo;
	
	@JsonProperty("propertyCriteria")
	private PropertyCriteria propertyCriteria;
}

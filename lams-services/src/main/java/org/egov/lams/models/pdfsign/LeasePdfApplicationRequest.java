package org.egov.lams.models.pdfsign;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;

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
public class LeasePdfApplicationRequest {
	
	@JsonProperty("RequestInfo")
    private RequestInfo requestInfo = null;

	@JsonProperty("LeaseApplication")
	private List<LeasePdfApplication> LeaseApplication;
}

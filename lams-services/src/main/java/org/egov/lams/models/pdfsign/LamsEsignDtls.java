package org.egov.lams.models.pdfsign;

import org.egov.lams.web.models.AuditDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class LamsEsignDtls {
	@JsonProperty("id")
	public String id;
	@JsonProperty("fileStoreId")
	public String fileStoreId;
	@JsonProperty("txnId")
	public String txnId;
	@JsonProperty("auditDetails")
	public AuditDetails auditDetails;
	@JsonProperty("status")
	public String status;
	@JsonProperty("errorCode")
	public String errorCode;
	@JsonProperty("surveyId")
	public String surveyId;
	
}

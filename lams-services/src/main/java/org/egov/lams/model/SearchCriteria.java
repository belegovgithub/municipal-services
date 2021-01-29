package org.egov.lams.model;

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

	@JsonProperty("ids")
	private List<String> ids;
	
	@JsonProperty("tenantIds")
	private List<String> tenantIds;
	
	@JsonProperty("userIds")
	private List<String> userIds;

	@JsonProperty("applicationNumber")
	private String applicationNumber;
	
	@JsonProperty("accountId")
	private String accountId;

	@JsonProperty("mobileNumber")
	private String mobileNumber;
	
	@JsonProperty("businessService")
	private String businessService;
	
	//@JsonProperty("userIds")
	//private List<String> userIds;
	
	@JsonProperty("offset")
	private Integer offset;

	@JsonProperty("limit")
	private Integer limit;
	
	@JsonProperty("status")
    private String status;

	@JsonProperty("applicationType")
	private String applicationType;
	
	@JsonProperty("located")
	private String located;

	@JsonProperty("type")
	private String type;
	
	@JsonProperty("surveyNo")
	private String surveyNo;
	
	@JsonProperty("termNo")
	private String termNo;
	
	@JsonProperty("fromDate")
    private Long fromDate = null;

    @JsonProperty("toDate")
    private Long toDate = null;
    
    @JsonProperty("surveyId")
	private String surveyId;
	
	@Override
	public String toString() {
		return "SearchCriteria [tenantId=" + tenantId + ", ids=" + ids + ", userIds=" + userIds + ",applicationNumber=" + applicationNumber
				+ ", accountId=" + accountId + ", mobileNumber=" + mobileNumber + ", businessService=" + businessService
				+ ", offset=" + offset + ", limit=" + limit + ", status=" + status + "]";
	}

	public boolean isEmpty() {
		return (this.tenantId == null && this.status == null && this.applicationType == null && this.ids == null && this.applicationNumber == null
                &&  this.mobileNumber == null && this.userIds==null
        );
	}
	
	
}

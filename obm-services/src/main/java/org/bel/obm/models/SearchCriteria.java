package org.bel.obm.models;

import java.util.List;

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

	private String tenantId;

	private List<String> ids;
	
	private List<String> tenantIds;
	
	private List<String> userIds;

	private String applicationNumber;
	
	private String accountId;

	private String mobileNumber;
	
	private String businessService;
	
	private Integer offset;

	private Integer limit;
	
    private String status;

    private Long fromDate = null;

    private Long toDate = null;

	public String toString() {
		return "SearchCriteria [tenantId=" + tenantId + ", ids=" + ids + ", tenantIds=" + tenantIds + ", userIds="
				+ userIds + ", applicationNumber=" + applicationNumber + ", accountId=" + accountId + ", mobileNumber="
				+ mobileNumber + ", businessService=" + businessService + ", offset=" + offset + ", limit=" + limit
				+ ", status=" + status + ", fromDate=" + fromDate + ", toDate=" + toDate + "]";
	}
	
	public boolean isEmpty() {
		return (this.tenantId == null && this.status == null && this.ids == null && this.applicationNumber == null
                &&  this.mobileNumber == null && this.userIds==null
        );
	}
}

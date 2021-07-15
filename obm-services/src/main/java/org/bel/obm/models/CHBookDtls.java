package org.bel.obm.models;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;

import org.egov.common.contract.request.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CHBookDtls {

	@Size(max = 64)
	private String id;

	@Size(max = 64)
	private String tenantId;

	private Long fromDate;
    private Long toDate;
    private String purpose;
    private String purposeDescription;
    private String category;
    private String residentTypeId; 
    private String timeSlotId ;
    
    private String accountId;
    private String businessService;
    private AuditDetails auditDetails;
    
    @Size(max = 64)
	private String applicationNumber;
	private Long applicationDate;
	private Long approvedDate;
	private List<Document> applicationDocuments = null;

	@Size(max = 64)
	private String action;
	
	private String comment;
	private List<String> assignee;
	private List<Document> wfDocuments;
	
	@Size(max = 64)
	private String status;
	
    private String workflowCode = null;
    
    private User userDetails;
    
    private BankDetails bankDetails;
    
    private String hallId;
    
    public CHBookDtls addApplicationDocumentsItem(Document applicationDocumentsItem) {
        if (this.applicationDocuments == null) {
        this.applicationDocuments = new ArrayList<>();
        }
        if(!this.applicationDocuments.contains(applicationDocumentsItem))
            this.applicationDocuments.add(applicationDocumentsItem);
        return this;
    }

	@Override
	public String toString() {
		return "CHBookDtls [id=" + id + ", tenantId=" + tenantId + ", fromDate=" + fromDate + ", toDate=" + toDate
				+ ", purpose=" + purpose + ", category=" + category + ", accountId=" + accountId + ", businessService="
				+ businessService + ", auditDetails=" + auditDetails + ", applicationNumber=" + applicationNumber
				+ ", applicationDate=" + applicationDate + ", approvedDate=" + approvedDate + ", applicationDocuments="
				+ applicationDocuments + ", action=" + action + ", comment=" + comment + ", assignee=" + assignee
				+ ", wfDocuments=" + wfDocuments + ", status=" + status + ", workflowCode=" + workflowCode
				+ ", userDetails=" + userDetails + ", bankDetails=" + bankDetails + ", hallId=" + hallId + "]";
	}
}

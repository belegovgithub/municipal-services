package org.bel.obm.util;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bel.obm.models.AuditDetails;
import org.bel.obm.models.CHBookDtls;
import org.bel.obm.workflow.WorkflowService;
import org.bel.obm.workflow.models.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.Getter;
@Component
@Getter
public class CommonUtils {

	
	@Autowired
	private WorkflowService workflowService;
	
	/**
	 * Method to return auditDetails for create/update flows
	 *
	 * @param by
	 * @param isCreate
	 * @return AuditDetails
	 */
    public AuditDetails getAuditDetails(String by, Boolean isCreate) {
    	
        Long time = System.currentTimeMillis();
        
        if(isCreate)
            return AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time).build();
        else
            return AuditDetails.builder().lastModifiedBy(by).lastModifiedTime(time).build();
    }
    
    public Map<String, Boolean> getIdToIsStateUpdatableMap(BusinessService businessService, List<CHBookDtls> searchresult) {
        Map<String, Boolean> idToIsStateUpdatableMap = new HashMap<>();
        searchresult.forEach(result -> {
            idToIsStateUpdatableMap.put(result.getId(), workflowService.isStateUpdatable(result.getStatus(), businessService));
        });
        return idToIsStateUpdatableMap;
    }
}

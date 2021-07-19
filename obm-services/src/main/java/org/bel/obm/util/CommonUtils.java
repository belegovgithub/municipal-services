package org.bel.obm.util;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bel.obm.constants.OBMConfiguration;
import org.bel.obm.models.AuditDetails;
import org.bel.obm.models.CHBookDtls;
import org.bel.obm.repository.ServiceRequestRepository;
import org.bel.obm.workflow.WorkflowService;
import org.bel.obm.workflow.models.BusinessService;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Getter
public class CommonUtils {

	
	@Autowired
	private WorkflowService workflowService;
	
	@Autowired
	private OBMConfiguration config;
	
	@Autowired
	private ServiceRequestRepository serviceRequestRepository;
	
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
    
    public MdmsCriteriaReq prepareMdMsRequest(String tenantId, String moduleName, List<String> names, String filter,
			RequestInfo requestInfo) {

		List<MasterDetail> masterDetails = new ArrayList<>();
		names.forEach(name -> {
			masterDetails.add(MasterDetail.builder().name(name).filter(filter).build());
		});

		ModuleDetail moduleDetail = ModuleDetail.builder().moduleName(moduleName).masterDetails(masterDetails).build();
		List<ModuleDetail> moduleDetails = new ArrayList<>();
		moduleDetails.add(moduleDetail);
		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().tenantId(tenantId).moduleDetails(moduleDetails).build();
		return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
	}

	
	public DocumentContext getAttributeValues(MdmsCriteriaReq mdmsReq) {
		StringBuilder uri = new StringBuilder(config.getMdmsHost()).append(config.getMdmsEndpoint());

		try {
			Object resp = serviceRequestRepository.fetchResult(uri, mdmsReq);
			return JsonPath.parse(resp);
		} catch (Exception e) {
			log.error("Error while fetching MDMS data", e);
			throw new CustomException("INVALID_INPUT", "Invalid Input Data");
		}
	}
}

package org.bel.obm.workflow;

import org.bel.obm.constants.OBMConfiguration;
import org.bel.obm.models.RequestInfoWrapper;
import org.bel.obm.repository.ServiceRequestRepository;
import org.bel.obm.workflow.models.BusinessService;
import org.bel.obm.workflow.models.BusinessServiceResponse;
import org.bel.obm.workflow.models.State;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WorkflowService {

	private OBMConfiguration config;

	private ServiceRequestRepository serviceRequestRepository;

	private ObjectMapper mapper;

	@Autowired
	public WorkflowService(OBMConfiguration config, ServiceRequestRepository serviceRequestRepository,
			ObjectMapper mapper) {
		this.config = config;
		this.serviceRequestRepository = serviceRequestRepository;
		this.mapper = mapper;
	}

	public BusinessService getBusinessService(String tenantId, RequestInfo requestInfo, String businessServiceName) {

		StringBuilder url = getSearchURLWithParams(tenantId, businessServiceName);
		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
		Object result = serviceRequestRepository.fetchResult(url, requestInfoWrapper);
		BusinessServiceResponse response = null;
		try {
			response = mapper.convertValue(result, BusinessServiceResponse.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException("PARSING ERROR", "Failed to parse response of calculate");
		}
		return response.getBusinessServices().get(0);
	}

	private StringBuilder getSearchURLWithParams(String tenantId, String businessServiceName) {
		StringBuilder url = new StringBuilder(config.getWfHost());
		url.append(config.getWfBusinessServiceSearchPath());
		url.append("?tenantId=");
		url.append(tenantId);
		url.append("&businessServices=");
		url.append(businessServiceName);
		return url;
	}

	public Boolean isStateUpdatable(String stateCode, BusinessService businessService) {
		for (State state : businessService.getStates()) {
			if (state.getApplicationStatus() != null && state.getApplicationStatus().equalsIgnoreCase(stateCode))
				return state.getIsStateUpdatable();
		}
		return null;
	}

}

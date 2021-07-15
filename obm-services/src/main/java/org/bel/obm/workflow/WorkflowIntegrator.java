package org.bel.obm.workflow;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bel.obm.constants.OBMConfiguration;
import org.bel.obm.models.CHBookDtls;
import org.bel.obm.models.CHBookRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Service
@Slf4j
public class WorkflowIntegrator {

	private static final String TENANTIDKEY = "tenantId";

	private static final String BUSINESSSERVICEKEY = "businessService";

	private static final String ACTIONKEY = "action";

	private static final String COMMENTKEY = "comment";

	private static final String MODULENAMEKEY = "moduleName";

	private static final String BUSINESSIDKEY = "businessId";

	private static final String DOCUMENTSKEY = "documents";

	private static final String ASSIGNEEKEY = "assignes";

	private static final String UUIDKEY = "uuid";

	private static final String MODULENAMEVALUE = "obm-services";

	private static final String WORKFLOWREQUESTARRAYKEY = "ProcessInstances";

	private static final String REQUESTINFOKEY = "RequestInfo";

	private static final String PROCESSINSTANCESJOSNKEY = "$.ProcessInstances";

	private static final String BUSINESSIDJOSNKEY = "$.businessId";

	private static final String STATUSJSONKEY = "$.state.applicationStatus";

	private RestTemplate rest;

	private OBMConfiguration config;

	@Autowired
	public WorkflowIntegrator(RestTemplate rest, OBMConfiguration config) {
		this.rest = rest;
		this.config = config;
	}

	public void callWorkFlow(CHBookRequest chBookRequest) {
		CHBookDtls chBookDtls = chBookRequest.getBooking();
		String wfTenantId = chBookDtls.getTenantId();
		JSONArray array = new JSONArray();
		JSONObject obj = new JSONObject();
		List<Map<String, String>> uuidmaps = new LinkedList<>();
		if (!CollectionUtils.isEmpty(chBookDtls.getAssignee())) {

			chBookDtls.getAssignee().forEach(assignee -> {
				Map<String, String> uuidMap = new HashMap<>();
				uuidMap.put(UUIDKEY, assignee);
				uuidmaps.add(uuidMap);
			});
		}
		obj.put(BUSINESSIDKEY, chBookDtls.getApplicationNumber());
		obj.put(TENANTIDKEY, wfTenantId);
		obj.put(BUSINESSSERVICEKEY, chBookDtls.getWorkflowCode());
		obj.put(MODULENAMEKEY, MODULENAMEVALUE);

		obj.put(ACTIONKEY, chBookDtls.getAction());
		obj.put(COMMENTKEY, chBookDtls.getComment());
		if (!CollectionUtils.isEmpty(chBookDtls.getAssignee()))
			obj.put(ASSIGNEEKEY, uuidmaps);
		obj.put(DOCUMENTSKEY, chBookDtls.getWfDocuments());
		array.add(obj);
		if (!array.isEmpty()) {
			JSONObject workFlowRequest = new JSONObject();
			workFlowRequest.put(REQUESTINFOKEY, chBookRequest.getRequestInfo());
			workFlowRequest.put(WORKFLOWREQUESTARRAYKEY, array);
			String response = null;
			try {
				response = rest.postForObject(config.getWfHost().concat(config.getWfTransitionPath()), workFlowRequest,
						String.class);
			} catch (HttpClientErrorException e) {

				DocumentContext responseContext = JsonPath.parse(e.getResponseBodyAsString());
				List<Object> errros = null;
				try {
					errros = responseContext.read("$.Errors");
				} catch (PathNotFoundException pnfe) {
					log.error("EG_TL_WF_ERROR_KEY_NOT_FOUND",
							" Unable to read the json path in error object : " + pnfe.getMessage());
					throw new CustomException("EG_TL_WF_ERROR_KEY_NOT_FOUND",
							" Unable to read the json path in error object : " + pnfe.getMessage());
				}
				throw new CustomException("EG_WF_ERROR", errros.toString());
			} catch (Exception e) {
				throw new CustomException("EG_WF_ERROR",
						" Exception occured while integrating with workflow : " + e.getMessage());
			}

			DocumentContext responseContext = JsonPath.parse(response);
			List<Map<String, Object>> responseArray = responseContext.read(PROCESSINSTANCESJOSNKEY);
			Map<String, String> idStatusMap = new HashMap<>();
			responseArray.forEach(object -> {

				DocumentContext instanceContext = JsonPath.parse(object);
				idStatusMap.put(instanceContext.read(BUSINESSIDJOSNKEY), instanceContext.read(STATUSJSONKEY));
			});

			chBookRequest.getBooking()
					.setStatus(idStatusMap.get(chBookRequest.getBooking().getApplicationNumber()));
		}
	}
}
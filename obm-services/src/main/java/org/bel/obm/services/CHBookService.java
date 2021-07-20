package org.bel.obm.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bel.obm.constants.OBMConstant;
import org.bel.obm.models.CHBookDtls;
import org.bel.obm.models.CHBookRequest;
import org.bel.obm.models.SearchCriteria;
import org.bel.obm.repository.OBMRepository;
import org.bel.obm.user.UserDetailResponse;
import org.bel.obm.util.CommonUtils;
import org.bel.obm.validators.CHBookValidator;
import org.bel.obm.workflow.ActionValidator;
import org.bel.obm.workflow.WorkflowIntegrator;
import org.bel.obm.workflow.WorkflowService;
import org.bel.obm.workflow.models.BusinessService;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Service
public class CHBookService {

	@Autowired
	private CHBookValidator validator;

	@Autowired
	private CHBookEnrichmentService enrichmentService;

	@Autowired
	private WorkflowIntegrator wfIntegrator;

	@Autowired
	private OBMRepository repository;

	@Autowired
	private UserService userService;

	@Autowired
	private ActionValidator actionValidator;

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	private CommonUtils util;

	public CHBookDtls create(CHBookRequest request) {
		validator.validateFields(request);
		validator.validateBusinessService(request);
		validator.validateAndEncrichMDMSData(request);
		enrichmentService.enrichCreateRequest(request);
		userService.createUser(request);
		repository.save(request);
		wfIntegrator.callWorkFlow(request);
		return request.getBooking();
	}

	public List<CHBookDtls> search(SearchCriteria criteria, RequestInfo requestInfo) {
		List<CHBookDtls> chBookDtlsList = null;
		enrichmentService.enrichSearchCriteriaWithAccountId(requestInfo, criteria);

		if (criteria.getMobileNumber() != null) {
			UserDetailResponse userDetailResponse = userService.getUser(criteria, requestInfo);
			if (userDetailResponse.getUser().size() == 0) {
				return Collections.emptyList();
			}
			if (CollectionUtils.isEmpty(criteria.getUserIds())) {
				Set<String> ownerids = new HashSet<>();
				userDetailResponse.getUser().forEach(owner -> ownerids.add(owner.getUuid()));
				criteria.setUserIds(new ArrayList<>(ownerids));
			}
		}

		chBookDtlsList = repository.getCHBookDts(criteria);
		chBookDtlsList.forEach(chBookDtls -> {
			UserDetailResponse userDetailResponse = userService.getUserByUUid(chBookDtls.getAccountId(), requestInfo);
			chBookDtls.setUserDetails(userDetailResponse.getUser().get(0));
			validator.validateUserwithOwnerDetail(requestInfo, chBookDtls);
		});
		return chBookDtlsList;
	}

	public CHBookDtls update(CHBookRequest chBookRequest) {
		String businessServiceName = chBookRequest.getBooking().getWorkflowCode();
		BusinessService businessService = workflowService.getBusinessService(
				chBookRequest.getBooking().getTenantId(), chBookRequest.getRequestInfo(), businessServiceName);
		actionValidator.validateUpdateRequest(chBookRequest, businessService);
		enrichmentService.enrichCHBookUpdateRequest(chBookRequest, businessService);
		List<CHBookDtls> searchResult = getCHBWithInfo(chBookRequest);
		validator.validateUpdate(chBookRequest, searchResult);

		Map<String, Boolean> idToIsStateUpdatableMap = util.getIdToIsStateUpdatableMap(businessService, searchResult);

		wfIntegrator.callWorkFlow(chBookRequest);
		enrichmentService.postStatusEnrichment(chBookRequest);
		repository.update(chBookRequest, idToIsStateUpdatableMap);
		return chBookRequest.getBooking();
	}

	public List<CHBookDtls> getCHBWithInfo(CHBookRequest request) {
		SearchCriteria criteria = new SearchCriteria();
		List<String> ids = new LinkedList<>();
		CHBookDtls chBookDtls = request.getBooking();
		ids.add(chBookDtls.getId());

		criteria.setTenantId(chBookDtls.getTenantId());
		criteria.setIds(ids);
		criteria.setBusinessService(chBookDtls.getBusinessService());

		List<CHBookDtls> chBookDtlsList = repository.getCHBookDts(criteria);

		if (chBookDtlsList.isEmpty())
			return Collections.emptyList();
		return chBookDtlsList;
	}

	public String bookedHistory(SearchCriteria criteria, RequestInfo requestInfo) {
		List<CHBookDtls> chBookDtlsList = null;
		validator.validateBookedHistory(criteria);
		enrichmentService.enrichFromAndToBookedDate(criteria);
		chBookDtlsList = repository.getCHBookDts(criteria);
		JsonArray jsArrayBooked = new JsonArray();
		for(CHBookDtls chBookDtls :chBookDtlsList) {
			JsonArray jsArray = new JsonArray();
			jsArray.add(chBookDtls.getFromDate());
			jsArray.add(chBookDtls.getToDate());
			jsArrayBooked.addAll(jsArray);
		}
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("booked", jsArrayBooked);
		JsonObject returnObject = new JsonObject();
		returnObject.add("reservedSlots", jsonObject);
		return new Gson().toJson(returnObject);
	}
}

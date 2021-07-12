package org.bel.obm.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bel.obm.models.CHBookDtls;
import org.bel.obm.models.CHBookRequest;
import org.bel.obm.models.SearchCriteria;
import org.bel.obm.repository.OBMRepository;
import org.bel.obm.user.UserDetailResponse;
import org.bel.obm.validators.CHBookValidator;
import org.bel.obm.workflow.WorkflowIntegrator;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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

	public CHBookDtls create(CHBookRequest request) {
		validator.validateFields(request);
		validator.validateBusinessService(request);
		enrichmentService.enrichCreateRequest(request);
		userService.createUser(request);
		repository.save(request);
		wfIntegrator.callWorkFlow(request);
		return request.getCHBookDtls();
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
}

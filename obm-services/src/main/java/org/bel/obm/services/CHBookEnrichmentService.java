package org.bel.obm.services;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bel.obm.Idgen.IdResponse;
import org.bel.obm.constants.OBMConfiguration;
import org.bel.obm.constants.OBMConstant;
import org.bel.obm.models.AuditDetails;
import org.bel.obm.models.CHBookDtls;
import org.bel.obm.models.CHBookRequest;
import org.bel.obm.models.Document;
import org.bel.obm.models.SearchCriteria;
import org.bel.obm.repository.IdGenRepository;
import org.bel.obm.repository.OBMRepository;
import org.bel.obm.util.CommonUtils;
import org.bel.obm.workflow.WorkflowService;
import org.bel.obm.workflow.models.BusinessService;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class CHBookEnrichmentService {

	@Autowired
	private IdGenRepository idGenRepository;
	@Autowired
	private OBMConfiguration config;
	@Autowired
	private CommonUtils commUtils;
	@Autowired
	private WorkflowService workflowService;
	@Autowired
	private OBMRepository repository;

	public void enrichCreateRequest(CHBookRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		String uuid = requestInfo.getUserInfo().getUuid();
		AuditDetails auditDetails = commUtils.getAuditDetails(uuid, true);
		CHBookDtls chBookDtls = request.getBooking();
		chBookDtls.setAuditDetails(auditDetails);
		chBookDtls.setId(UUID.randomUUID().toString());
		chBookDtls.getBankDetails().setId(UUID.randomUUID().toString());
		chBookDtls.setStatus(OBMConstant.APPLIED);
		if (requestInfo.getUserInfo().getType().equalsIgnoreCase(OBMConstant.ROLE_CITIZEN))
			chBookDtls.setAccountId(requestInfo.getUserInfo().getUuid().toString());
		chBookDtls.setApplicationDate(auditDetails.getCreatedTime());
		chBookDtls.getApplicationDocuments().forEach(document -> {
			document.setId(UUID.randomUUID().toString());
			document.setActive(true);
		});
		setIdgenIds(request);
	}

	private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey, String idformat, int count) {
		List<IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, idformat, count)
				.getIdResponses();

		if (CollectionUtils.isEmpty(idResponses))
			throw new CustomException("IDGEN ERROR", "No ids returned from idgen Service");

		return idResponses.stream().map(IdResponse::getId).collect(Collectors.toList());
	}

	private void setIdgenIds(CHBookRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		String tenantId = request.getBooking().getTenantId();
		String applicationNumber = "";
		applicationNumber = getIdList(requestInfo, tenantId, config.getObmCHBookApplNumIdgenName(),
				config.getObmCHBookApplNumIdgenFormat(), 1).get(0);
		request.getBooking().setApplicationNumber(applicationNumber);
	}

	public void enrichSearchCriteriaWithAccountId(RequestInfo requestInfo, SearchCriteria criteria) {
		if (criteria.isEmpty() && requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN")) {
			criteria.setAccountId(requestInfo.getUserInfo().getUuid().toString());
			criteria.setMobileNumber(requestInfo.getUserInfo().getUserName());
			criteria.setTenantId(requestInfo.getUserInfo().getTenantId());
		}
	}

	public void enrichCHBookUpdateRequest(CHBookRequest chBookRequest, BusinessService businessService) {
		RequestInfo requestInfo = chBookRequest.getRequestInfo();
		AuditDetails auditDetails = commUtils.getAuditDetails(requestInfo.getUserInfo().getUuid(), false);
		CHBookDtls chBookBtls = chBookRequest.getBooking();
		chBookBtls.setAuditDetails(auditDetails);
		enrichAssignes(chBookBtls);
		String nameOfBusinessService = chBookBtls.getBusinessService();
		if (nameOfBusinessService == null) {
			nameOfBusinessService = OBMConstant.businessService_OBM;
			chBookBtls.setBusinessService(nameOfBusinessService);
		}
		if (workflowService.isStateUpdatable(chBookBtls.getStatus(), businessService)) {
			chBookBtls.getBankDetails().setAuditDetails(auditDetails);

			if (!CollectionUtils.isEmpty(chBookBtls.getApplicationDocuments())) {
				List<String> docIdstoDelete = new ArrayList<String>();
				List<Document> docsToDelete = new ArrayList<Document>();

				chBookBtls.getApplicationDocuments().forEach(document -> {
					if (document.getId() == null) {
						document.setId(UUID.randomUUID().toString());
						document.setActive(true);
					} else if (!document.getActive()) {
						docsToDelete.add(document);
						docIdstoDelete.add(document.getId());
					}
				});
				// docIdsStored.removeAll(docIdsRecived);
				if (docsToDelete.size() > 0) {
					repository.deleteApplDocs(docIdstoDelete);
					chBookBtls.getApplicationDocuments().removeAll(docsToDelete);
				}
			}
		}
	}

	public void enrichAssignes(CHBookDtls chBookDtls) {
		if (chBookDtls.getAction().equalsIgnoreCase(OBMConstant.CITIZEN_SENDBACK_ACTION)) {
			List<String> assignes = new LinkedList<>();
			if (chBookDtls.getAccountId() != null)
				assignes.add(chBookDtls.getAccountId());
			chBookDtls.setAssignee(assignes);
		}
	}

	public void postStatusEnrichment(CHBookRequest request) {
		CHBookDtls chBookDtls = request.getBooking();
		if ((chBookDtls.getStatus() != null) && (chBookDtls.getStatus().equalsIgnoreCase(OBMConstant.STATUS_APPROVED) || 
				chBookDtls.getStatus().equalsIgnoreCase(OBMConstant.STATUS_REJECTED) || chBookDtls.getStatus().equalsIgnoreCase(OBMConstant.STATUS_CANCELLED))) {
			Long time = System.currentTimeMillis();
			chBookDtls.setApprovedDate(time);
		}
	}
}

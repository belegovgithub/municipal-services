package org.bel.obm.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bel.obm.Idgen.IdResponse;
import org.bel.obm.constants.OBMConfiguration;
import org.bel.obm.constants.OBMConstant;
import org.bel.obm.models.AuditDetails;
import org.bel.obm.models.CHBookDtls;
import org.bel.obm.models.CHBookRequest;
import org.bel.obm.models.SearchCriteria;
import org.bel.obm.repository.IdGenRepository;
import org.bel.obm.util.CommonUtils;
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
	
	public void enrichCreateRequest(CHBookRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		String uuid = requestInfo.getUserInfo().getUuid();
		AuditDetails auditDetails = commUtils.getAuditDetails(uuid, true);
		CHBookDtls chBookDtls = request.getCHBookDtls();
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
		String tenantId = request.getCHBookDtls().getTenantId();
		String applicationNumber = "123456";
		applicationNumber = getIdList(requestInfo, tenantId, config.getObmCHBookApplNumIdgenName(),
					config.getObmCHBookApplNumIdgenFormat(), 1).get(0);
		request.getCHBookDtls().setApplicationNumber(applicationNumber);
	}
	
	public void enrichSearchCriteriaWithAccountId(RequestInfo requestInfo, SearchCriteria criteria) {
		if(criteria.isEmpty() && requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN")){
            criteria.setAccountId(requestInfo.getUserInfo().getUuid().toString());
            criteria.setMobileNumber(requestInfo.getUserInfo().getUserName());
            criteria.setTenantId(requestInfo.getUserInfo().getTenantId());
        }
	}
}

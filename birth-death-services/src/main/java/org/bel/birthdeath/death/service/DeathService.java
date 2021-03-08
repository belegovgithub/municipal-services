package org.bel.birthdeath.death.service;

import java.util.List;

import org.bel.birthdeath.common.contract.DeathPdfApplicationRequest;
import org.bel.birthdeath.common.contract.EgovPdfResp;
import org.bel.birthdeath.common.model.AuditDetails;
import org.bel.birthdeath.death.certmodel.DeathCertAppln;
import org.bel.birthdeath.death.certmodel.DeathCertRequest;
import org.bel.birthdeath.death.certmodel.DeathCertificate;
import org.bel.birthdeath.death.certmodel.DeathCertificate.StatusEnum;
import org.bel.birthdeath.death.model.EgDeathDtl;
import org.bel.birthdeath.death.model.SearchCriteria;
import org.bel.birthdeath.death.repository.DeathRepository;
import org.bel.birthdeath.death.validator.DeathValidator;
import org.bel.birthdeath.utils.CommonUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeathService {
	
	@Autowired
	DeathRepository repository;

	@Autowired
	DeathValidator validator;
	
	@Autowired
	EnrichmentServiceDeath enrichmentServiceDeath;
	
	@Autowired
	CalculationServiceDeath calculationServiceDeath;
	
	@Autowired
	CommonUtils commUtils;
	
	public List<EgDeathDtl> search(SearchCriteria criteria) {
		List<EgDeathDtl> deathDtls = null ;
		if(validator.validateFields(criteria))
			deathDtls = repository.getDeathDtls(criteria);
		return deathDtls;
	}

	public DeathCertificate download(SearchCriteria criteria, RequestInfo requestInfo) {
		DeathCertificate deathCertificate = new DeathCertificate();
		deathCertificate.setDeathDtlId(criteria.getId());
		deathCertificate.setTenantId(criteria.getTenantId());
		DeathCertRequest deathCertRequest = DeathCertRequest.builder().deathCertificate(deathCertificate).requestInfo(requestInfo).build();
		List<EgDeathDtl> birtDtls = repository.getDeathDtlsAll(criteria);
		if(birtDtls.size()>1) 
			throw new CustomException("Invalid_Input","Error in processing data");
		enrichmentServiceDeath.enrichCreateRequest(deathCertRequest);
		enrichmentServiceDeath.setIdgenIds(deathCertRequest);
		if(birtDtls.get(0).getCounter()>0){
			enrichmentServiceDeath.setDemandParams(deathCertRequest);
			enrichmentServiceDeath.setGLCode(deathCertRequest);
			calculationServiceDeath.addCalculation(deathCertRequest);
			deathCertificate.setApplicationStatus(StatusEnum.ACTIVE);
		}
		else{
			DeathPdfApplicationRequest applicationRequest = DeathPdfApplicationRequest.builder().requestInfo(requestInfo).DeathCertificate(birtDtls).build();
			EgovPdfResp pdfResp = repository.saveDeathCertPdf(applicationRequest);
			deathCertificate.setFilestoreid(pdfResp.getFilestoreIds().get(0));
			repository.updateCounter(deathCertificate.getDeathDtlId());
			deathCertificate.setApplicationStatus(StatusEnum.FREE_DOWNLOAD);
			
		}
		deathCertificate.setCounter(birtDtls.get(0).getCounter());
		repository.save(deathCertRequest);
		return deathCertificate;
	}

	public DeathCertificate getDeathCertReqByConsumerCode(SearchCriteria criteria, RequestInfo requestInfo) {
		return repository.getDeathCertReqByConsumerCode(criteria.getConsumerCode());
	}
	
	public List<DeathCertAppln> searchApplications(RequestInfo requestInfo) {
		List<DeathCertAppln> certApplns=null;
		certApplns = repository.searchApplications(requestInfo.getUserInfo().getUuid());
		return certApplns;
	}

	public void updateDownloadStatus(DeathCertRequest certRequest) {
		AuditDetails auditDetails = commUtils.getAuditDetails(certRequest.getRequestInfo().getUserInfo().getUuid(), false);
		DeathCertificate deathCert = certRequest.getDeathCertificate();
		deathCert.setAuditDetails(auditDetails);
		deathCert.setApplicationStatus(StatusEnum.PAID_DOWNLOAD);
		repository.updateDownloadStatus(certRequest);
	}
}

package org.bel.birthdeath.birth.service;

import java.util.List;

import org.bel.birthdeath.birth.certmodel.BirthCertAppln;
import org.bel.birthdeath.birth.certmodel.BirthCertRequest;
import org.bel.birthdeath.birth.certmodel.BirthCertificate;
import org.bel.birthdeath.birth.certmodel.BirthCertificate.StatusEnum;
import org.bel.birthdeath.birth.model.EgBirthDtl;
import org.bel.birthdeath.birth.model.SearchCriteria;
import org.bel.birthdeath.birth.repository.BirthRepository;
import org.bel.birthdeath.birth.validator.BirthValidator;
import org.bel.birthdeath.common.contract.BirthPdfApplicationRequest;
import org.bel.birthdeath.common.contract.EgovPdfResp;
import org.bel.birthdeath.common.model.AuditDetails;
import org.bel.birthdeath.utils.CommonUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BirthService {
	
	@Autowired
	BirthRepository repository;

	@Autowired
	BirthValidator validator;
	
	@Autowired
	EnrichmentService enrichmentService;
	
	@Autowired
	CalculationService calculationService;
	
	@Autowired
	CommonUtils commUtils;
	
	public List<EgBirthDtl> search(SearchCriteria criteria) {
		List<EgBirthDtl> birthDtls = null ;
		if(validator.validateFields(criteria)) 
			birthDtls = repository.getBirthDtls(criteria);
		return birthDtls;
	}

	public BirthCertificate download(SearchCriteria criteria, RequestInfo requestInfo) {
		try {
		BirthCertificate birthCertificate = new BirthCertificate();
		birthCertificate.setBirthDtlId(criteria.getId());
		birthCertificate.setTenantId(criteria.getTenantId());
		BirthCertRequest birthCertRequest = BirthCertRequest.builder().birthCertificate(birthCertificate).requestInfo(requestInfo).build();
		List<EgBirthDtl> birtDtls = repository.getBirthDtlsAll(criteria,requestInfo);
		if(birtDtls.size()>1) 
			throw new CustomException("Invalid_Input","Error in processing data");
		enrichmentService.enrichCreateRequest(birthCertRequest);
		enrichmentService.setIdgenIds(birthCertRequest);
		if(birtDtls.get(0).getCounter()>0){
			enrichmentService.setDemandParams(birthCertRequest);
			enrichmentService.setGLCode(birthCertRequest);
			calculationService.addCalculation(birthCertRequest);
			birthCertificate.setApplicationStatus(StatusEnum.ACTIVE);
		}
		else{
			birtDtls.get(0).setBirthcertificateno(birthCertRequest.getBirthCertificate().getBirthCertificateNo());
			BirthPdfApplicationRequest applicationRequest = BirthPdfApplicationRequest.builder().requestInfo(requestInfo).birthCertificate(birtDtls).build();
			EgovPdfResp pdfResp = repository.saveBirthCertPdf(applicationRequest);
			birthCertificate.setEmbeddedUrl(applicationRequest.getBirthCertificate().get(0).getEmbeddedUrl());
			birthCertificate.setDateofissue(applicationRequest.getBirthCertificate().get(0).getDateofissue());
			birthCertificate.setFilestoreid(pdfResp.getFilestoreIds().get(0));
			repository.updateCounter(birthCertificate.getBirthDtlId());
			birthCertificate.setApplicationStatus(StatusEnum.FREE_DOWNLOAD);
			
		}
		birthCertificate.setCounter(birtDtls.get(0).getCounter());
		repository.save(birthCertRequest);
		return birthCertificate;
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new CustomException("DOWNLOAD_ERROR","Error in Downloading Certificate");
		}
	}

	public BirthCertificate getBirthCertReqByConsumerCode(SearchCriteria criteria, RequestInfo requestInfo) {
		return repository.getBirthCertReqByConsumerCode(criteria.getConsumerCode(),requestInfo);
	}
	
	public List<BirthCertAppln> searchApplications(RequestInfo requestInfo) {
		List<BirthCertAppln> certApplns=null;
		certApplns = repository.searchApplications(requestInfo.getUserInfo().getUuid());
		return certApplns;
	}

	public void updateDownloadStatus(BirthCertRequest certRequest) {
		if(null!=certRequest.getRequestInfo() && null!=certRequest.getRequestInfo().getUserInfo() && null!=certRequest.getRequestInfo().getUserInfo().getUuid())
		{
			AuditDetails auditDetails = commUtils.getAuditDetails(certRequest.getRequestInfo().getUserInfo().getUuid(), false);
			BirthCertificate birthCert = certRequest.getBirthCertificate();
			birthCert.getAuditDetails().setLastModifiedBy(auditDetails.getLastModifiedBy());
			birthCert.getAuditDetails().setLastModifiedTime(auditDetails.getLastModifiedTime());
			//birthCert.setAuditDetails(auditDetails);
			birthCert.setApplicationStatus(StatusEnum.PAID_DOWNLOAD);
			repository.update(certRequest);
		}
	}

	public List<EgBirthDtl> viewCertificateData(SearchCriteria criteria) {
		return repository.viewCertificateData(criteria);
	}
	
	public List<EgBirthDtl> viewfullCertMasterData(SearchCriteria criteria,RequestInfo requestInfo) {
		return repository.viewfullCertMasterData(criteria,requestInfo);
	}
}

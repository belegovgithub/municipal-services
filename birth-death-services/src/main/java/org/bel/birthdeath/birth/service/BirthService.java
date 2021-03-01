package org.bel.birthdeath.birth.service;

import java.util.List;

import org.bel.birthdeath.birth.certmodel.BirthCertRequest;
import org.bel.birthdeath.birth.certmodel.BirthCertificate;
import org.bel.birthdeath.birth.model.EgBirthDtl;
import org.bel.birthdeath.birth.model.SearchCriteria;
import org.bel.birthdeath.birth.repository.BirthRepository;
import org.bel.birthdeath.birth.validator.BirthValidator;
import org.bel.birthdeath.common.contract.BirthPdfApplicationRequest;
import org.bel.birthdeath.common.contract.EgovPdfResp;
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
	
	public List<EgBirthDtl> search(SearchCriteria criteria) {
		List<EgBirthDtl> birthDtls = null ;
		if(validator.validateFields(criteria))
			birthDtls = repository.getBirthDtls(criteria);
		return birthDtls;
	}

	public BirthCertificate download(SearchCriteria criteria, RequestInfo requestInfo) {
		BirthCertificate birthCertificate = new BirthCertificate();
		birthCertificate.setBirthDtlId(criteria.getId());
		birthCertificate.setTenantId(criteria.getTenantId());
		BirthCertRequest birthCertRequest = BirthCertRequest.builder().birthCertificate(birthCertificate).requestInfo(requestInfo).build();
		List<EgBirthDtl> birtDtls = repository.getBirthDtlsAll(criteria);
		if(birtDtls.size()>1) 
			throw new CustomException("Invalid_Input","Error in processing data");
		enrichmentService.enrichCreateRequest(birthCertRequest);
		enrichmentService.setIdgenIds(birthCertRequest);
		if(birtDtls.get(0).getCounter()>0){
			enrichmentService.setDemandParams(birthCertRequest);
			enrichmentService.setGLCode(birthCertRequest);
			calculationService.addCalculation(birthCertRequest);
		}
		else{
			BirthPdfApplicationRequest applicationRequest = BirthPdfApplicationRequest.builder().requestInfo(requestInfo).BirthCertificate(birtDtls).build();
			EgovPdfResp pdfResp = repository.saveBirthCertPdf(applicationRequest);
			birthCertificate.setFilestoreid(pdfResp.getFilestoreIds().get(0));
			repository.updateCounter(birthCertificate.getBirthDtlId());
			
		}
		birthCertificate.setCounter(birtDtls.get(0).getCounter());
		repository.save(birthCertRequest);
		return birthCertificate;
	}
}

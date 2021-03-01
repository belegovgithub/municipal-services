package org.bel.birthdeath.common.services;

import java.util.List;

import org.bel.birthdeath.birth.certmodel.BirthCertificate;
import org.bel.birthdeath.birth.model.SearchCriteria;
import org.bel.birthdeath.birth.repository.BirthRepository;
import org.bel.birthdeath.common.model.EgHospitalDtl;
import org.bel.birthdeath.common.repository.CommonRepository;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommonService {
	
	@Autowired
	CommonRepository repository;
	
	@Autowired
	BirthRepository birthRepository;
	
	public List<EgHospitalDtl> search(String tenantId) {
		List<EgHospitalDtl> hospitalDtls = null ;
			hospitalDtls = repository.getHospitalDtls(tenantId);
		return hospitalDtls;
	}

	public BirthCertificate getBirthCertReqByConsumerCode(SearchCriteria criteria, RequestInfo requestInfo) {
		return birthRepository.getBirthCertReqByConsumerCode(criteria.getConsumerCode());
	}

}

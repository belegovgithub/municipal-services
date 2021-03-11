package org.bel.birthdeath.common.services;

import java.util.ArrayList;
import java.util.List;

import org.bel.birthdeath.birth.model.EgBirthDtl;
import org.bel.birthdeath.common.contract.BirthResponse;
import org.bel.birthdeath.common.model.EgHospitalDtl;
import org.bel.birthdeath.common.repository.CommonRepository;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommonService {
	
	@Autowired
	CommonRepository repository;
	
	public List<EgHospitalDtl> search(String tenantId) {
		List<EgHospitalDtl> hospitalDtls = new ArrayList<EgHospitalDtl>() ;
		hospitalDtls = repository.getHospitalDtls(tenantId);
		return hospitalDtls;
	}

	public List<EgBirthDtl> saveBirthImport(BirthResponse importJSon, RequestInfo requestInfo) {
		ArrayList<EgBirthDtl> birthDtls = new ArrayList<EgBirthDtl>();
		birthDtls = repository.saveBirthImport(importJSon, requestInfo);
		return birthDtls;
	}

}

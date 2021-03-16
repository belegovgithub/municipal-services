package org.bel.birthdeath.common.services;

import java.util.ArrayList;
import java.util.List;

import org.bel.birthdeath.common.contract.BirthResponse;
import org.bel.birthdeath.common.contract.DeathResponse;
import org.bel.birthdeath.common.model.EgHospitalDtl;
import org.bel.birthdeath.common.model.ImportBndWrapper;
import org.bel.birthdeath.common.repository.CommonRepository;
import org.bel.birthdeath.death.model.EgDeathDtl;
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

	public ImportBndWrapper saveBirthImport(BirthResponse importJSon, RequestInfo requestInfo) {
		ImportBndWrapper birthDtls = repository.saveBirthImport(importJSon, requestInfo);
		return birthDtls;
	}
	
	public ImportBndWrapper saveDeathImport(DeathResponse importJSon, RequestInfo requestInfo) {
		ImportBndWrapper deathDtls = repository.saveDeathImport(importJSon, requestInfo);
		return deathDtls;
	}

}

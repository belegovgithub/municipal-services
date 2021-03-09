package org.bel.birthdeath.birth.validator;

import org.bel.birthdeath.birth.model.EgBirthDtl;
import org.bel.birthdeath.birth.model.SearchCriteria;
import org.bel.birthdeath.birth.repository.BirthRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BirthValidator {
	
	@Autowired
	BirthRepository repository;

	public boolean validateFields(SearchCriteria criteria) {
		if (criteria.getTenantId() == null || criteria.getTenantId().isEmpty() || criteria.getGender() == null
				|| criteria.getDateOfBirth().isEmpty() || criteria.getDateOfBirth() == null)
			throw new CustomException("null_input", "Mandatory fileds can not be empty.");
		/*if ((criteria.getRegistrationNo() == null || criteria.getRegistrationNo().isEmpty())
				&& (criteria.getHospitalname() == null || criteria.getHospitalname().isEmpty() ||
						criteria.getMotherName() == null || criteria.getMotherName().isEmpty() ))
			throw new CustomException("null_input", "Search criteria not meeting.");*/
		return true;
	}

	public boolean validateUniqueRegNo(EgBirthDtl birthDtl) {
		SearchCriteria criteria = new SearchCriteria();
		criteria.setRegistrationNo(birthDtl.getRegistrationno());
		criteria.setTenantId(birthDtl.getTenantid());
		if(repository.getBirthDtls(criteria).size()==0)
			return true;
		return false;
	}
}

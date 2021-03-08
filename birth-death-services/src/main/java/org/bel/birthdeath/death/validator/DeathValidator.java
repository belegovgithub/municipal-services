package org.bel.birthdeath.death.validator;

import org.bel.birthdeath.death.model.SearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

@Component
public class DeathValidator {

	public boolean validateFields(SearchCriteria criteria) {
		if (criteria.getTenantId() == null || criteria.getTenantId().isEmpty() || criteria.getGender() == null
				|| criteria.getDateOfDeath().isEmpty() || criteria.getDateOfDeath() == null)
			throw new CustomException("null_input", "Mandatory fileds can not be empty.");
		/*if ((criteria.getRegistrationNo() == null || criteria.getRegistrationNo().isEmpty())
				&& (criteria.getHospitalname() == null || criteria.getHospitalname().isEmpty() ||
						criteria.getMotherName() == null || criteria.getMotherName().isEmpty() ))
			throw new CustomException("null_input", "Search criteria not meeting.");*/
		return true;
	}
}

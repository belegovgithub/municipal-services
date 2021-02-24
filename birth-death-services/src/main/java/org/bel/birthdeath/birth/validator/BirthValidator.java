package org.bel.birthdeath.birth.validator;

import org.bel.birthdeath.birth.model.SearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

@Component
public class BirthValidator {

	public boolean validateFields(SearchCriteria criteria) {
		if (criteria.getTenantId() == null || criteria.getTenantId().isEmpty() || criteria.getGender() == null
				|| criteria.getDateofbirth().isEmpty() || criteria.getDateofbirth() == null)
			throw new CustomException("null_input", "Mandatory fileds can not be empty.");
		if ((criteria.getRegistrationNo() == null || criteria.getRegistrationNo().isEmpty())
				&& (criteria.getHospitalname() == null || criteria.getHospitalname().isEmpty() ||
						criteria.getMotherName() == null || criteria.getMotherName().isEmpty() ))
			throw new CustomException("null_input", "Search criteria not meeting.");
		return true;
	}
}

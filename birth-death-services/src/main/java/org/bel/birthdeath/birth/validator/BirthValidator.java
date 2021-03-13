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
				|| criteria.getDateOfBirth() == null || criteria.getDateOfBirth().isEmpty() )
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
		birthDtl.setRejectReason("Reg No already exists");
		return false;
	}
	
	public boolean validateImportFields(EgBirthDtl birthDtl) {
		if(birthDtl.getTenantid()==null || birthDtl.getTenantid().isEmpty() ) {
			birthDtl.setRejectReason("Tenantid cannot be empty");
			return false;
		}
		if(birthDtl.getRegistrationno()==null || birthDtl.getRegistrationno().isEmpty()) {
			birthDtl.setRejectReason("Reg No cannot be empty");
			return false;
		}
		if(birthDtl.getDateofbirth()==null) {
			birthDtl.setRejectReason("DoB cannot be empty");
			return false;
		}
		if(birthDtl.getGender()==null) {
			birthDtl.setRejectReason("Gender cannot be empty");
			return false;
		}
		if(birthDtl.getGender().intValue()!=1 && birthDtl.getGender().intValue()!=2 && birthDtl.getGender().intValue()!=3 ) {
			birthDtl.setRejectReason("Gender value is not in range (1:3)");
			return false;
		}
		if(birthDtl.getFirstname()!=null && birthDtl.getFirstname().length()>200) {
			birthDtl.setRejectReason("Firstname cannot exceed 200 chars");
			return false;
		}
		if(birthDtl.getMiddlename()!=null && birthDtl.getMiddlename().length()>200) {
			birthDtl.setRejectReason("Middlename cannot exceed 200 chars");
			return false;
		}
		if(birthDtl.getLastname()!=null && birthDtl.getLastname().length()>200) {
			birthDtl.setRejectReason("Lastname cannot exceed 200 chars");
			return false;
		}
		if(birthDtl.getBirthFatherInfo().getFirstname()!=null && birthDtl.getBirthFatherInfo().getFirstname().length()>200) {
			birthDtl.setRejectReason("Father Firstname cannot exceed 200 chars");
			return false;
		}
		if(birthDtl.getBirthFatherInfo().getMiddlename()!=null && birthDtl.getBirthFatherInfo().getMiddlename().length()>200) {
			birthDtl.setRejectReason("Father Middlename cannot exceed 200 chars");
			return false;
		}
		if(birthDtl.getBirthFatherInfo().getLastname()!=null && birthDtl.getBirthFatherInfo().getLastname().length()>200) {
			birthDtl.setRejectReason("Father Lastname cannot exceed 200 chars");
			return false;
		}
		if(birthDtl.getBirthMotherInfo().getFirstname()!=null && birthDtl.getBirthMotherInfo().getFirstname().length()>200) {
			birthDtl.setRejectReason("Mother Firstname cannot exceed 200 chars");
			return false;
		}
		if(birthDtl.getBirthMotherInfo().getMiddlename()!=null && birthDtl.getBirthMotherInfo().getMiddlename().length()>200) {
			birthDtl.setRejectReason("Mother Middlename cannot exceed 200 chars");
			return false;
		}
		if(birthDtl.getBirthMotherInfo().getLastname()!=null && birthDtl.getBirthMotherInfo().getLastname().length()>200) {
			birthDtl.setRejectReason("Mother Lastname cannot exceed 200 chars");
			return false;
		}
		return true;
	}
}

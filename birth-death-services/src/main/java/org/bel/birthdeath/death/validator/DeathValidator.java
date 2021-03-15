package org.bel.birthdeath.death.validator;

import org.bel.birthdeath.death.model.EgDeathDtl;
import org.bel.birthdeath.death.model.SearchCriteria;
import org.bel.birthdeath.death.repository.DeathRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeathValidator {

	@Autowired
	DeathRepository repository;
	
	public boolean validateFields(SearchCriteria criteria) {
		if (criteria.getTenantId() == null || criteria.getTenantId().isEmpty() || criteria.getGender() == null
			|| criteria.getDateOfDeath() == null	|| criteria.getDateOfDeath().isEmpty() )
			throw new CustomException("null_input", "Mandatory fileds can not be empty.");
		/*if ((criteria.getRegistrationNo() == null || criteria.getRegistrationNo().isEmpty())
				&& (criteria.getHospitalname() == null || criteria.getHospitalname().isEmpty() ||
						criteria.getMotherName() == null || criteria.getMotherName().isEmpty() ))
			throw new CustomException("null_input", "Search criteria not meeting.");*/
		return true;
	}
	
	public boolean validateUniqueRegNo(EgDeathDtl deathDtl) {
		SearchCriteria criteria = new SearchCriteria();
		criteria.setRegistrationNo(deathDtl.getRegistrationno());
		criteria.setTenantId(deathDtl.getTenantid());
		if(repository.getDeathDtls(criteria).size()==0)
			return true;
		deathDtl.setRejectReason("Reg No already exists");
		return false;
	}
	
	public boolean validateImportFields(EgDeathDtl deathDtl) {
		if(deathDtl.getTenantid()==null || deathDtl.getTenantid().isEmpty() ) {
			deathDtl.setRejectReason("Tenantid cannot be empty");
			return false;
		}
		if(deathDtl.getRegistrationno()==null || deathDtl.getRegistrationno().isEmpty()) {
			deathDtl.setRejectReason("Reg No cannot be empty");
			return false;
		}
		if(deathDtl.getDateofdeath()==null) {
			deathDtl.setRejectReason("DoD cannot be empty");
			return false;
		}
		if(deathDtl.getGender()==null) {
			deathDtl.setRejectReason("Gender cannot be empty");
			return false;
		}
		if(deathDtl.getGender().intValue()!=1 && deathDtl.getGender().intValue()!=2 && deathDtl.getGender().intValue()!=3 ) {
			deathDtl.setRejectReason("Gender value is not in range (1:3)");
			return false;
		}
		if(deathDtl.getFirstname()!=null && deathDtl.getFirstname().length()>200) {
			deathDtl.setRejectReason("Firstname cannot exceed 200 chars");
			return false;
		}
		if(deathDtl.getMiddlename()!=null && deathDtl.getMiddlename().length()>200) {
			deathDtl.setRejectReason("Middlename cannot exceed 200 chars");
			return false;
		}
		if(deathDtl.getLastname()!=null && deathDtl.getLastname().length()>200) {
			deathDtl.setRejectReason("Lastname cannot exceed 200 chars");
			return false;
		}
		if(deathDtl.getDeathFatherInfo().getFirstname()!=null && deathDtl.getDeathFatherInfo().getFirstname().length()>200) {
			deathDtl.setRejectReason("Father Firstname cannot exceed 200 chars");
			return false;
		}
		if(deathDtl.getDeathFatherInfo().getMiddlename()!=null && deathDtl.getDeathFatherInfo().getMiddlename().length()>200) {
			deathDtl.setRejectReason("Father Middlename cannot exceed 200 chars");
			return false;
		}
		if(deathDtl.getDeathFatherInfo().getLastname()!=null && deathDtl.getDeathFatherInfo().getLastname().length()>200) {
			deathDtl.setRejectReason("Father Lastname cannot exceed 200 chars");
			return false;
		}
		if(deathDtl.getDeathMotherInfo().getFirstname()!=null && deathDtl.getDeathMotherInfo().getFirstname().length()>200) {
			deathDtl.setRejectReason("Mother Firstname cannot exceed 200 chars");
			return false;
		}
		if(deathDtl.getDeathMotherInfo().getMiddlename()!=null && deathDtl.getDeathMotherInfo().getMiddlename().length()>200) {
			deathDtl.setRejectReason("Mother Middlename cannot exceed 200 chars");
			return false;
		}
		if(deathDtl.getDeathMotherInfo().getLastname()!=null && deathDtl.getDeathMotherInfo().getLastname().length()>200) {
			deathDtl.setRejectReason("Mother Lastname cannot exceed 200 chars");
			return false;
		}
		if(deathDtl.getDeathSpouseInfo().getFirstname()!=null && deathDtl.getDeathSpouseInfo().getFirstname().length()>200) {
			deathDtl.setRejectReason("Spouse Firstname cannot exceed 200 chars");
			return false;
		}
		if(deathDtl.getDeathSpouseInfo().getMiddlename()!=null && deathDtl.getDeathSpouseInfo().getMiddlename().length()>200) {
			deathDtl.setRejectReason("Spouse Middlename cannot exceed 200 chars");
			return false;
		}
		if(deathDtl.getDeathSpouseInfo().getLastname()!=null && deathDtl.getDeathSpouseInfo().getLastname().length()>200) {
			deathDtl.setRejectReason("Spouse Lastname cannot exceed 200 chars");
			return false;
		}
		return true;
	}
}

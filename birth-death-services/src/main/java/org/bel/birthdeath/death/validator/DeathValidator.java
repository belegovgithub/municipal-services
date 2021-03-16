package org.bel.birthdeath.death.validator;

import org.bel.birthdeath.birth.model.EgBirthDtl;
import org.bel.birthdeath.birth.model.ImportBirthWrapper;
import org.bel.birthdeath.death.model.EgDeathDtl;
import org.bel.birthdeath.death.model.ImportDeathWrapper;
import org.bel.birthdeath.death.model.SearchCriteria;
import org.bel.birthdeath.death.repository.DeathRepository;
import org.bel.birthdeath.utils.BirthDeathConstants;
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
	
	public boolean validateUniqueRegNo(EgDeathDtl deathDtl,ImportDeathWrapper importDeathWrapper) {
		SearchCriteria criteria = new SearchCriteria();
		criteria.setRegistrationNo(deathDtl.getRegistrationno());
		criteria.setTenantId(deathDtl.getTenantid());
		if(repository.getDeathDtls(criteria).size()==0)
			return true;
		deathDtl.setRejectReason(BirthDeathConstants.DUPLICATE_REG);
		importDeathWrapper.updateMaps(BirthDeathConstants.DUPLICATE_REG, deathDtl);
		return false;
	}
	
	public boolean validateImportFields(EgDeathDtl deathDtl,ImportDeathWrapper importDeathWrapper) {
		if(deathDtl.getTenantid()==null || deathDtl.getTenantid().isEmpty() ) {
			setRejectionReason(BirthDeathConstants.TENANT_EMPTY,deathDtl,importDeathWrapper);
			return false;
		}
		if(deathDtl.getRegistrationno()==null || deathDtl.getRegistrationno().isEmpty()) {
			setRejectionReason(BirthDeathConstants.REG_EMPTY,deathDtl,importDeathWrapper);
			return false;
		}
		if(deathDtl.getDateofdeath()==null) {
			setRejectionReason(BirthDeathConstants.DOD_EMPTY,deathDtl,importDeathWrapper);
			return false;
		}
		if(deathDtl.getGender()==null) {
			setRejectionReason(BirthDeathConstants.GENDER_EMPTY,deathDtl,importDeathWrapper);
			return false;
		}
		if(deathDtl.getGender().intValue()!=1 && deathDtl.getGender().intValue()!=2 && deathDtl.getGender().intValue()!=3 ) {
			setRejectionReason(BirthDeathConstants.GENDER_INVALID,deathDtl,importDeathWrapper);
			return false;
		}
		if(deathDtl.getFirstname()!=null && deathDtl.getFirstname().length()>200) {
			setRejectionReason(BirthDeathConstants.FIRSTNAME_LARGE,deathDtl,importDeathWrapper);
			return false;
		}
		if(deathDtl.getMiddlename()!=null && deathDtl.getMiddlename().length()>200) {
			setRejectionReason(BirthDeathConstants.MIDDLENAME_LARGE,deathDtl,importDeathWrapper);
			return false;
		}
		if(deathDtl.getLastname()!=null && deathDtl.getLastname().length()>200) {
			setRejectionReason(BirthDeathConstants.LASTNAME_LARGE,deathDtl,importDeathWrapper);
			return false;
		}
		if(deathDtl.getDeathFatherInfo().getFirstname()!=null && deathDtl.getDeathFatherInfo().getFirstname().length()>200) {
			setRejectionReason(BirthDeathConstants.F_FIRSTNAME_LARGE,deathDtl,importDeathWrapper);
			return false;
		}
		if(deathDtl.getDeathFatherInfo().getMiddlename()!=null && deathDtl.getDeathFatherInfo().getMiddlename().length()>200) {
			setRejectionReason(BirthDeathConstants.F_MIDDLENAME_LARGE,deathDtl,importDeathWrapper);
			return false;
		}
		if(deathDtl.getDeathFatherInfo().getLastname()!=null && deathDtl.getDeathFatherInfo().getLastname().length()>200) {
			setRejectionReason(BirthDeathConstants.F_LASTNAME_LARGE,deathDtl,importDeathWrapper);
			return false;
		}
		if(deathDtl.getDeathMotherInfo().getFirstname()!=null && deathDtl.getDeathMotherInfo().getFirstname().length()>200) {
			setRejectionReason(BirthDeathConstants.M_FIRSTNAME_LARGE,deathDtl,importDeathWrapper);
			return false;
		}
		if(deathDtl.getDeathMotherInfo().getMiddlename()!=null && deathDtl.getDeathMotherInfo().getMiddlename().length()>200) {
			setRejectionReason(BirthDeathConstants.M_MIDDLENAME_LARGE,deathDtl,importDeathWrapper);
			return false;
		}
		if(deathDtl.getDeathMotherInfo().getLastname()!=null && deathDtl.getDeathMotherInfo().getLastname().length()>200) {
			setRejectionReason(BirthDeathConstants.M_LASTNAME_LARGE,deathDtl,importDeathWrapper);
			return false;
		}
		if(deathDtl.getDeathSpouseInfo().getFirstname()!=null && deathDtl.getDeathSpouseInfo().getFirstname().length()>200) {
			setRejectionReason(BirthDeathConstants.S_FIRSTNAME_LARGE,deathDtl,importDeathWrapper);
			return false;
		}
		if(deathDtl.getDeathSpouseInfo().getMiddlename()!=null && deathDtl.getDeathSpouseInfo().getMiddlename().length()>200) {
			setRejectionReason(BirthDeathConstants.S_MIDDLENAME_LARGE,deathDtl,importDeathWrapper);
			return false;
		}
		if(deathDtl.getDeathSpouseInfo().getLastname()!=null && deathDtl.getDeathSpouseInfo().getLastname().length()>200) {
			setRejectionReason(BirthDeathConstants.S_LASTNAME_LARGE,deathDtl,importDeathWrapper);
			return false;
		}
		return true;
	}
	
	private void setRejectionReason(String reason,EgDeathDtl egDeathDtl,ImportDeathWrapper importDeathWrapper)
	{
		egDeathDtl.setRejectReason(reason);
		importDeathWrapper.updateMaps(reason, egDeathDtl);
	}
}

package org.bel.birthdeath.birth.validator;

import java.sql.Timestamp;

import org.bel.birthdeath.birth.model.EgBirthDtl;
import org.bel.birthdeath.birth.model.ImportBirthWrapper;
import org.bel.birthdeath.birth.model.SearchCriteria;
import org.bel.birthdeath.birth.repository.BirthRepository;
import org.bel.birthdeath.utils.BirthDeathConstants;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BirthValidator {
	
	@Autowired
	BirthRepository repository;
	
	Timestamp beforeDate =  new Timestamp(1640975400000l);
	Timestamp afterDate = new Timestamp(-2209008070000l);
	
	public boolean validateFields(SearchCriteria criteria) {
		if (criteria.getTenantId() == null || criteria.getTenantId().isEmpty() || criteria.getGender() == null
				|| criteria.getDateOfBirth() == null || criteria.getDateOfBirth().isEmpty() )
			throw new CustomException("null_input", BirthDeathConstants.MANDATORY_MISSING);
		/*if ((criteria.getRegistrationNo() == null || criteria.getRegistrationNo().isEmpty())
				&& (criteria.getHospitalname() == null || criteria.getHospitalname().isEmpty() ||
						criteria.getMotherName() == null || criteria.getMotherName().isEmpty() ))
			throw new CustomException("null_input", "Search criteria not meeting.");*/
		return true;
	}

	public boolean validateUniqueRegNo(EgBirthDtl birthDtl,ImportBirthWrapper importBirthWrapper) {
		SearchCriteria criteria = new SearchCriteria();
		criteria.setRegistrationNo(birthDtl.getRegistrationno());
		criteria.setTenantId(birthDtl.getTenantid());
		if(repository.getBirthDtls(criteria).size()==0)
			return true;
		birthDtl.setRejectReason(BirthDeathConstants.DUPLICATE_REG);
		importBirthWrapper.updateMaps(BirthDeathConstants.DUPLICATE_REG, birthDtl);
		return false;
	}
	
	public boolean validateImportFields(EgBirthDtl birthDtl,ImportBirthWrapper importBirthWrapper) {
		if(null!=birthDtl.getDateofbirthepoch() )
		{
			Long dobepoch = null;
			try
			{
				dobepoch = Long.parseLong(birthDtl.getDateofbirthepoch());
			}
			catch (NumberFormatException e) {
				birthDtl.setRejectReason(BirthDeathConstants.INVALID_DOB);
				importBirthWrapper.updateMaps(BirthDeathConstants.INVALID_DOB, birthDtl);
				return false;
			}
			if(dobepoch!=null)
			{
				Timestamp dobepochTimestamp = new Timestamp(dobepoch*1000);
				if(!(dobepochTimestamp.before(beforeDate) && dobepochTimestamp.after(afterDate)))
				{
					birthDtl.setRejectReason(BirthDeathConstants.INVALID_DOB_RANGE);
					importBirthWrapper.updateMaps(BirthDeathConstants.INVALID_DOB_RANGE, birthDtl);
					return false;
				}
				birthDtl.setDateofbirth(dobepochTimestamp);
			}
		}
		if(null!=birthDtl.getDateofreportepoch() )
		{
			Long dorepoch = null;
			try
			{
				dorepoch = Long.parseLong(birthDtl.getDateofreportepoch());
			}
			catch (NumberFormatException e) {
				birthDtl.setRejectReason(BirthDeathConstants.INVALID_DOR);
				importBirthWrapper.updateMaps(BirthDeathConstants.INVALID_DOR, birthDtl);
				return false;
			}
			if(dorepoch!=null)
			{
				Timestamp dorepochTimestamp = new Timestamp(dorepoch*1000);
				if(!(dorepochTimestamp.before(beforeDate) && dorepochTimestamp.after(afterDate)))
				{
					birthDtl.setRejectReason(BirthDeathConstants.INVALID_DOR_RANGE);
					importBirthWrapper.updateMaps(BirthDeathConstants.INVALID_DOR_RANGE, birthDtl);
					return false;
				}
				birthDtl.setDateofreport(dorepochTimestamp);
			}
		}
		if(birthDtl.getTenantid()==null || birthDtl.getTenantid().isEmpty() ) {
			setRejectionReason(BirthDeathConstants.TENANT_EMPTY,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getRegistrationno()==null || birthDtl.getRegistrationno().isEmpty()) {
			setRejectionReason(BirthDeathConstants.REG_EMPTY,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getDateofbirth()==null) {
			setRejectionReason(BirthDeathConstants.DOB_EMPTY,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getGender()==null) {
			setRejectionReason(BirthDeathConstants.GENDER_EMPTY,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getGender().intValue()!=1 && birthDtl.getGender().intValue()!=2 && birthDtl.getGender().intValue()!=3 ) {
			setRejectionReason(BirthDeathConstants.GENDER_INVALID,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getFirstname()!=null && birthDtl.getFirstname().length()>200) {
			setRejectionReason(BirthDeathConstants.FIRSTNAME_LARGE,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getMiddlename()!=null && birthDtl.getMiddlename().length()>200) {
			setRejectionReason(BirthDeathConstants.MIDDLENAME_LARGE,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getLastname()!=null && birthDtl.getLastname().length()>200) {
			setRejectionReason(BirthDeathConstants.LASTNAME_LARGE,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthFatherInfo().getFirstname()!=null && birthDtl.getBirthFatherInfo().getFirstname().length()>200) {
			setRejectionReason(BirthDeathConstants.F_FIRSTNAME_LARGE,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthFatherInfo().getMiddlename()!=null && birthDtl.getBirthFatherInfo().getMiddlename().length()>200) {
			setRejectionReason(BirthDeathConstants.F_MIDDLENAME_LARGE,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthFatherInfo().getLastname()!=null && birthDtl.getBirthFatherInfo().getLastname().length()>200) {
			setRejectionReason(BirthDeathConstants.F_LASTNAME_LARGE,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthMotherInfo().getFirstname()!=null && birthDtl.getBirthMotherInfo().getFirstname().length()>200) {
			setRejectionReason(BirthDeathConstants.M_FIRSTNAME_LARGE,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthMotherInfo().getMiddlename()!=null && birthDtl.getBirthMotherInfo().getMiddlename().length()>200) {
			setRejectionReason(BirthDeathConstants.M_MIDDLENAME_LARGE,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthMotherInfo().getLastname()!=null && birthDtl.getBirthMotherInfo().getLastname().length()>200) {
			setRejectionReason(BirthDeathConstants.M_LASTNAME_LARGE,birthDtl,importBirthWrapper);
			return false;
		}
		return true;
	}
	
	private void setRejectionReason(String reason,EgBirthDtl birthDtl,ImportBirthWrapper importBirthWrapper)
	{
		birthDtl.setRejectReason(reason);
		importBirthWrapper.updateMaps(reason, birthDtl);
	}
}

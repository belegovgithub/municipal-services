package org.bel.birthdeath.birth.validator;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	Timestamp afterDate = new Timestamp(-5364683608000l);
	SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy");
	SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy");
	SimpleDateFormat sdf3 = new SimpleDateFormat("dd.MM.yyyy");
	
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
		
		if(null!=birthDtl.getDateofbirthepoch() && !birthDtl.getDateofbirthepoch().isEmpty())
		{
			Long dobdateFormatEpoch = dateFormatHandler(birthDtl.getDateofbirthepoch());
			if(null == dobdateFormatEpoch)
			{
				birthDtl.setRejectReason(BirthDeathConstants.INVALID_DOB);
				importBirthWrapper.updateMaps(BirthDeathConstants.INVALID_DOB, birthDtl);
				return false;
			}
			else
			{
				Timestamp dobdateRangeEpoch = dateTimeStampHandler(dobdateFormatEpoch);
				if(null==dobdateRangeEpoch) {
					birthDtl.setRejectReason(BirthDeathConstants.INVALID_DOB_RANGE);
					importBirthWrapper.updateMaps(BirthDeathConstants.INVALID_DOB_RANGE, birthDtl);
					return false;
				}
				birthDtl.setDateofbirth(dobdateRangeEpoch);
			}
		}

		if(null!=birthDtl.getDateofreportepoch() && !birthDtl.getDateofreportepoch().isEmpty())
		{
			Long dordateFormatEpoch = dateFormatHandler(birthDtl.getDateofreportepoch());
			if(null == dordateFormatEpoch)
			{
				birthDtl.setRejectReason(BirthDeathConstants.INVALID_DOR);
				importBirthWrapper.updateMaps(BirthDeathConstants.INVALID_DOR, birthDtl);
				return false;
			}
			else
			{
				Timestamp dordateRangeEpoch = dateTimeStampHandler(dordateFormatEpoch);
				if(null==dordateRangeEpoch) {
					birthDtl.setRejectReason(BirthDeathConstants.INVALID_DOR_RANGE);
					importBirthWrapper.updateMaps(BirthDeathConstants.INVALID_DOR_RANGE, birthDtl);
					return false;
				}
				birthDtl.setDateofreport(dordateRangeEpoch);
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
		if(birthDtl.getInformantsname()!=null && birthDtl.getInformantsname().length()>200) {
			setRejectionReason(BirthDeathConstants.INFORMANTNAME_LARGE,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getInformantsaddress()!=null && birthDtl.getInformantsaddress().length()>1000) {
			setRejectionReason(BirthDeathConstants.INFORMANTADDR_LARGE,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getPlaceofbirth()!=null && birthDtl.getPlaceofbirth().length()>1000) {
			setRejectionReason(BirthDeathConstants.PLACEOFBIRTH_LARGE,birthDtl,importBirthWrapper);
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
		if(birthDtl.getBirthFatherInfo().getEmailid()!=null && birthDtl.getBirthFatherInfo().getEmailid().length()>50) {
			setRejectionReason(BirthDeathConstants.F_EMAIL_LARGE,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthFatherInfo().getMobileno()!=null && birthDtl.getBirthFatherInfo().getMobileno().length()>20) {
			setRejectionReason(BirthDeathConstants.F_MOBILE_LARGE,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthFatherInfo().getAadharno()!=null && birthDtl.getBirthFatherInfo().getAadharno().length()>50) {
			setRejectionReason(BirthDeathConstants.F_AADHAR_LARGE,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthFatherInfo().getEducation()!=null && birthDtl.getBirthFatherInfo().getEducation().length()>100) {
			setRejectionReason(BirthDeathConstants.F_EDUCATION_LARGE,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthFatherInfo().getReligion()!=null && birthDtl.getBirthFatherInfo().getReligion().length()>100) {
			setRejectionReason(BirthDeathConstants.F_RELIGION_LARGE,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthFatherInfo().getProffession()!=null && birthDtl.getBirthFatherInfo().getProffession().length()>100) {
			setRejectionReason(BirthDeathConstants.F_PROFFESSION_LARGE,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthFatherInfo().getNationality()!=null && birthDtl.getBirthFatherInfo().getNationality().length()>100) {
			setRejectionReason(BirthDeathConstants.F_NATIONALITY_LARGE,birthDtl,importBirthWrapper);
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
		if(birthDtl.getBirthMotherInfo().getEmailid()!=null && birthDtl.getBirthMotherInfo().getEmailid().length()>50) {
			setRejectionReason(BirthDeathConstants.M_EMAIL_LARGE,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthMotherInfo().getMobileno()!=null && birthDtl.getBirthMotherInfo().getMobileno().length()>20) {
			setRejectionReason(BirthDeathConstants.M_MOBILE_LARGE,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthMotherInfo().getAadharno()!=null && birthDtl.getBirthMotherInfo().getAadharno().length()>50) {
			setRejectionReason(BirthDeathConstants.M_AADHAR_LARGE,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthMotherInfo().getEducation()!=null && birthDtl.getBirthMotherInfo().getEducation().length()>100) {
			setRejectionReason(BirthDeathConstants.M_EDUCATION_LARGE,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthMotherInfo().getReligion()!=null && birthDtl.getBirthMotherInfo().getReligion().length()>100) {
			setRejectionReason(BirthDeathConstants.M_RELIGION_LARGE,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthMotherInfo().getProffession()!=null && birthDtl.getBirthMotherInfo().getProffession().length()>100) {
			setRejectionReason(BirthDeathConstants.M_PROFFESSION_LARGE,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthMotherInfo().getNationality()!=null && birthDtl.getBirthMotherInfo().getNationality().length()>100) {
			setRejectionReason(BirthDeathConstants.M_NATIONALITY_LARGE,birthDtl,importBirthWrapper);
			return false;
		}
		
		if(birthDtl.getBirthPermaddr().getBuildingno()!=null && birthDtl.getBirthPermaddr().getBuildingno().length()>1000) {
			setRejectionReason(BirthDeathConstants.PERM_BUILDINGNO,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthPermaddr().getHouseno()!=null && birthDtl.getBirthPermaddr().getHouseno().length()>1000) {
			setRejectionReason(BirthDeathConstants.PERM_HOUSENO,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthPermaddr().getStreetname()!=null && birthDtl.getBirthPermaddr().getStreetname().length()>1000) {
			setRejectionReason(BirthDeathConstants.PERM_STREETNAME,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthPermaddr().getLocality()!=null && birthDtl.getBirthPermaddr().getLocality().length()>1000) {
			setRejectionReason(BirthDeathConstants.PERM_LOCALITY,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthPermaddr().getTehsil()!=null && birthDtl.getBirthPermaddr().getTehsil().length()>1000) {
			setRejectionReason(BirthDeathConstants.PERM_TEHSIL,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthPermaddr().getDistrict()!=null && birthDtl.getBirthPermaddr().getDistrict().length()>100) {
			setRejectionReason(BirthDeathConstants.PERM_DISTRICT,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthPermaddr().getCity()!=null && birthDtl.getBirthPermaddr().getCity().length()>100) {
			setRejectionReason(BirthDeathConstants.PERM_CITY,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthPermaddr().getState()!=null && birthDtl.getBirthPermaddr().getState().length()>100) {
			setRejectionReason(BirthDeathConstants.PERM_STATE,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthPermaddr().getPinno()!=null && birthDtl.getBirthPermaddr().getPinno().length()>100) {
			setRejectionReason(BirthDeathConstants.PERM_PINNO,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthPermaddr().getCountry()!=null && birthDtl.getBirthPermaddr().getCountry().length()>100) {
			setRejectionReason(BirthDeathConstants.PERM_COUNTRY,birthDtl,importBirthWrapper);
			return false;
		}
		
		if(birthDtl.getBirthPresentaddr().getBuildingno()!=null && birthDtl.getBirthPresentaddr().getBuildingno().length()>1000) {
			setRejectionReason(BirthDeathConstants.PRESENT_BUILDINGNO,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthPresentaddr().getHouseno()!=null && birthDtl.getBirthPresentaddr().getHouseno().length()>1000) {
			setRejectionReason(BirthDeathConstants.PRESENT_HOUSENO,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthPresentaddr().getStreetname()!=null && birthDtl.getBirthPresentaddr().getStreetname().length()>1000) {
			setRejectionReason(BirthDeathConstants.PRESENT_STREETNAME,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthPresentaddr().getLocality()!=null && birthDtl.getBirthPresentaddr().getLocality().length()>1000) {
			setRejectionReason(BirthDeathConstants.PRESENT_LOCALITY,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthPresentaddr().getTehsil()!=null && birthDtl.getBirthPresentaddr().getTehsil().length()>1000) {
			setRejectionReason(BirthDeathConstants.PRESENT_TEHSIL,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthPresentaddr().getDistrict()!=null && birthDtl.getBirthPresentaddr().getDistrict().length()>100) {
			setRejectionReason(BirthDeathConstants.PRESENT_DISTRICT,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthPresentaddr().getCity()!=null && birthDtl.getBirthPresentaddr().getCity().length()>100) {
			setRejectionReason(BirthDeathConstants.PRESENT_CITY,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthPresentaddr().getState()!=null && birthDtl.getBirthPresentaddr().getState().length()>100) {
			setRejectionReason(BirthDeathConstants.PRESENT_STATE,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthPresentaddr().getPinno()!=null && birthDtl.getBirthPresentaddr().getPinno().length()>100) {
			setRejectionReason(BirthDeathConstants.PRESENT_PINNO,birthDtl,importBirthWrapper);
			return false;
		}
		if(birthDtl.getBirthPresentaddr().getCountry()!=null && birthDtl.getBirthPresentaddr().getCountry().length()>100) {
			setRejectionReason(BirthDeathConstants.PRESENT_COUNTRY,birthDtl,importBirthWrapper);
			return false;
		}
		return true;
	}
	
	private void setRejectionReason(String reason,EgBirthDtl birthDtl,ImportBirthWrapper importBirthWrapper)
	{
		birthDtl.setRejectReason(reason);
		importBirthWrapper.updateMaps(reason, birthDtl);
	}
	
	private Long dateFormatHandler(String date)
	{
		Long timeLong = null;
		if(null!=date )
		{
			try
			{
				timeLong = Long.parseLong(date);
			}
			catch (NumberFormatException e) {
				try {
					timeLong = sdf1.parse(date).getTime();
					timeLong = timeLong/1000l;
				} catch (ParseException e1) {
					try {
						timeLong = sdf2.parse(date).getTime();
						timeLong = timeLong/1000l;
					} catch (ParseException e2) {
						try {
							timeLong = sdf3.parse(date).getTime();
							timeLong = timeLong/1000l;
						} catch (ParseException e3) {
							return null;
						}
					}
				}
			}
		}
		return timeLong;
	}
	
	private Timestamp dateTimeStampHandler(Long time)
	{
		Timestamp timeLongTimestamp = null;
		if(time!=null)
		{
			timeLongTimestamp = new Timestamp(time*1000);
			Timestamp beforeDate =  new Timestamp(System.currentTimeMillis()+10800000l);
			if(!(timeLongTimestamp.before(beforeDate) && timeLongTimestamp.after(afterDate)))
			{
				return null;
			}
		}
		return timeLongTimestamp;
	}
}


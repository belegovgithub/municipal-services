package org.bel.birthdeath.utils;

import org.springframework.stereotype.Component;

@Component
public class BirthDeathConstants {

    public static final String STATUS_ACTIVE = "ACTIVE";

    public static final String STATUS_CANCELLED  = "CANCELLED";

    public static final String STATUS_PAID  = "PAID";
    

    public static final String KEY_ID = "id";

    public static final String KEY_FILESTOREID = "filestoreid";

    public static final String KEY_PDF_JOBS = "jobs";

    public static final String KEY_PDF_ENTITY_ID = "entityid";

    public static final String KEY_PDF_FILESTOREID = "filestoreids";
    
    public static final String KEY_NAME = "key";
    
	public static final String GL_CODE_JSONPATH_CODE = "$.MdmsRes.BillingService.GLCode[?(@.code==\"{}\")]";

	public static final String GL_CODE = "glcode";
	
	public static final String GL_CODE_MASTER = "GLCode";

	public static final String BILLING_SERVICE = "BillingService";
	
	public static final String BIRTH_CERT = "BIRTH_CERT";
	
	public static final String BIRTH_CERT_FEE = "BIRTH_CERT_FEE";
	
	public static final String DEATH_CERT = "DEATH_CERT";
	
	public static final String DEATH_CERT_FEE = "DEATH_CERT_FEE";
	
	public static final String TENANT_EMPTY = "Tenantid cannot be empty";
	public static final String MANDATORY_MISSING = "DOB/GENDER is empty";
	public static final String DUPLICATE_REG = "Reg No already exists";
	public static final String DUPLICATE_REG_EXCEL = "Reg No already exists in Excel";
	public static final String HOSPNAME_LARGE = "Hospital name cannot exceed 500 chars";
	public static final String REG_EMPTY = "Reg No cannot be empty";
	public static final String DOB_EMPTY = "DoB cannot be empty";
	public static final String GENDER_EMPTY = "Gender cannot be empty";
	public static final String GENDER_INVALID = "Invalid Gender value";
	public static final String FIRSTNAME_LARGE = "Firstname cannot exceed 200 chars";
	public static final String MIDDLENAME_LARGE = "Middlename cannot exceed 200 chars";
	public static final String LASTNAME_LARGE = "Lastname cannot exceed 200 chars";
	public static final String F_FIRSTNAME_LARGE = "Father Firstname cannot exceed 200 chars";
	public static final String F_MIDDLENAME_LARGE = "Father Middlename cannot exceed 200 chars";
	public static final String F_LASTNAME_LARGE = "Father Lastname cannot exceed 200 chars";
	public static final String F_EDUCATION_LARGE = "Father Education cannot exceed 100 chars";
	public static final String F_RELIGION_LARGE = "Father Religion cannot exceed 100 chars";
	public static final String F_PROFFESSION_LARGE = "Father Profession cannot exceed 100 chars";
	public static final String F_NATIONALITY_LARGE = "Father Nationality cannot exceed 100 chars";
	public static final String M_EDUCATION_LARGE = "Mother Education cannot exceed 100 chars";
	public static final String M_RELIGION_LARGE = "Mother Religion cannot exceed 100 chars";
	public static final String M_PROFFESSION_LARGE = "Mother Profession cannot exceed 100 chars";
	public static final String M_NATIONALITY_LARGE = "Mother Nationality cannot exceed 100 chars";
	public static final String S_EDUCATION_LARGE = "Spouse Education cannot exceed 100 chars";
	public static final String S_RELIGION_LARGE = "Spouse Religion cannot exceed 100 chars";
	public static final String S_PROFFESSION_LARGE = "Spouse Profession cannot exceed 100 chars";
	public static final String S_NATIONALITY_LARGE = "Spouse Nationality cannot exceed 100 chars";
	public static final String M_FIRSTNAME_LARGE = "Mother Firstname cannot exceed 200 chars";
	public static final String M_MIDDLENAME_LARGE = "Mother Middlename cannot exceed 200 chars";
	public static final String M_LASTNAME_LARGE = "Mother Lastname cannot exceed 200 chars";
	public static final String F_EMAIL_LARGE = "Father Email cannot exceed 50 chars";
	public static final String M_EMAIL_LARGE = "Mother Email cannot exceed 50 chars";
	public static final String F_MOBILE_LARGE = "Father Mobile cannot exceed 20 chars";
	public static final String M_MOBILE_LARGE = "Mother Mobile cannot exceed 20 chars";
	public static final String F_AADHAR_LARGE = "Father Aadhar cannot exceed 50 chars";
	public static final String M_AADHAR_LARGE = "Mother Aadhar cannot exceed 50 chars";
	public static final String S_EMAIL_LARGE = "Spouse Email cannot exceed 50 chars";
	public static final String S_AADHAR_LARGE = "Spouse Aadhar cannot exceed 50 chars";
	public static final String S_MOBILE_LARGE = "Spouse Mobile cannot exceed 20 chars";
	public static final String INFORMANTNAME_LARGE = "Informants name cannot exceed 200 chars";
	public static final String INFORMANTADDR_LARGE = "Informants Address cannot exceed 1000 chars";
	public static final String PLACEOFBIRTH_LARGE = "Place of Birth cannot exceed 1000 chars";
	
	public static final String INVALID_DOB = "DOB not valid";
	public static final String INVALID_DOB_RANGE = "DOB not in range";
	public static final String INVALID_DOR = "DOR not valid";
	public static final String INVALID_DOR_RANGE = "DOR not in range";

	public static final String DOD_EMPTY = "DoD cannot be empty";
	public static final String INVALID_DOD = "DOD not valid";
	public static final String INVALID_DOD_RANGE = "DOD not in range";
	public static final String S_FIRSTNAME_LARGE = "Spouse Firstname cannot exceed 200 chars";
	public static final String S_MIDDLENAME_LARGE = "Spouse Middlename cannot exceed 200 chars";
	public static final String S_LASTNAME_LARGE = "Spouse Lastname cannot exceed 200 chars";
	public static final String PLACEOFDEATH_LARGE = "Place of Death cannot exceed 1000 chars";
	
	public static final String PERM_BUILDINGNO = "Building No in Permanent Address cannot exceed 1000 chars";
	public static final String PERM_HOUSENO = "House No in Permanent Address cannot exceed 1000 chars";
	public static final String PERM_STREETNAME = "Street name in Permanent Address cannot exceed 1000 chars";
	public static final String PERM_LOCALITY = "Locality in Permanent Address cannot exceed 1000 chars";
	public static final String PERM_TEHSIL = "Tehsil in Permanent Address cannot exceed 1000 chars";
	public static final String PERM_DISTRICT = "District in Permanent Address cannot exceed 100 chars";
	public static final String PERM_CITY = "City in Permanent Address cannot exceed 100 chars";
	public static final String PERM_STATE = "State in Permanent Address cannot exceed 100 chars";
	public static final String PERM_PINNO = "Pin No in Permanent Address cannot exceed 100 chars";
	public static final String PERM_COUNTRY = "Country in Permanent Address cannot exceed 100 chars";
	
	public static final String PRESENT_BUILDINGNO = "Building No in Present Address cannot exceed 1000 chars";
	public static final String PRESENT_HOUSENO = "House No in Present Address cannot exceed 1000 chars";
	public static final String PRESENT_STREETNAME = "Street name in Present Address cannot exceed 1000 chars";
	public static final String PRESENT_LOCALITY = "Locality in Present Address cannot exceed 1000 chars";
	public static final String PRESENT_TEHSIL = "Tehsil in Present Address cannot exceed 1000 chars";
	public static final String PRESENT_DISTRICT = "District in Present Address cannot exceed 100 chars";
	public static final String PRESENT_CITY = "City in Present Address cannot exceed 100 chars";
	public static final String PRESENT_STATE = "State in Present Address cannot exceed 100 chars";
	public static final String PRESENT_PINNO = "Pin No in Present Address cannot exceed 100 chars";
	public static final String PRESENT_COUNTRY = "Country in Present Address cannot exceed 100 chars";
	
	public static final String DATA_ERROR = "Data Error";
    public BirthDeathConstants() {}

}

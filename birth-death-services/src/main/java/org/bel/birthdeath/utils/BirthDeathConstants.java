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
	
    public BirthDeathConstants() {}

}

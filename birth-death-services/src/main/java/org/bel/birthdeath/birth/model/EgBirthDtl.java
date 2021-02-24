package org.bel.birthdeath.birth.model;

import java.sql.Timestamp;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class EgBirthDtl{

	private String id;

	private String createdby;

	private Long createdtime;

	private Timestamp dateofbirth;

	private Timestamp dateofreport;

	private String firstname;

	private Integer gender;

	private String hospitalname;

	private String informantsaddress;

	private String informantsname;

	private String lastname;

	private String middlename;

	private String placeofbirth;

	private String registrationno;

	private String remarks;

	private String lastmodifiedby;

	private Long lastmodifiedtime;

	private Integer counter;
	
	private String tenantid;
	
	private EgBirthFatherInfo birthFatherInfo;
	
	private EgBirthMotherInfo birthMotherInfo;
	
	private EgBirthPermaddr birthPermaddr;
	
	private EgBirthPresentaddr birthPresentaddr;
}
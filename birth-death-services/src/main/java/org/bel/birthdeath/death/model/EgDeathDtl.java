package org.bel.birthdeath.death.model;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EgDeathDtl{

	private String id;

	private String createdby;

	private Long createdtime;

	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd-MM-yyyy")
	private Timestamp dateofdeath;

	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd-MM-yyyy")
	private Timestamp dateofreport;
	
	private long dateofdeathepoch;

	private long dateofreportepoch;
	
	private Long dateofissue;

	private String firstname;

	private Integer gender;
	
	private String genderStr;

	private String hospitalname;

	private String informantsaddress;

	private String informantsname;

	private String lastname;

	private String middlename;

	private String placeofdeath;

	private String registrationno;

	private String remarks;

	private String lastmodifiedby;

	private Long lastmodifiedtime;

	private Integer counter;
	
	private String tenantid;
	
	private String hospitalid;
	
	private EgDeathFatherInfo deathFatherInfo;
	
	private EgDeathMotherInfo deathMotherInfo;
	
	private EgDeathPermaddr deathPermaddr;
	
	private EgDeathPresentaddr deathPresentaddr;
	
	private EgDeathSpouseInfo deathSpouseInfo;
	
	private String age;
	
	private String eidno;
	
	private String aadharno;
	
	private String nationality;
	
	private String religion;
	
	private String icdcode;	
	
	private String embeddedUrl;
	
	private String deathcertificateno;
	
	private String rejectReason;
	
	private String fullName;
}
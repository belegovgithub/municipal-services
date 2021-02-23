package org.bel.birthdeath.birth.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class EgBirthFatherInfo {

	private String id;

	private String aadharno;

	private String createdby;

	private Long createdon;

	private String education;

	private String emailid;

	private String firstname;

	private String lastname;

	private String middlename;

	private String mobileno;

	private String nationality;

	private String proffession;

	private String religion;

	private String updatedby;

	private Long updatedon;

}
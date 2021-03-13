package org.bel.birthdeath.death.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class EgDeathFatherInfo {

	private String id;

	private String aadharno;

	private String createdby;

	private Long createdtime;

	private String emailid;

	private String firstname;

	private String lastname;

	private String middlename;

	private String mobileno;

	private String lastmodifiedby;

	private Long lastmodifiedtime;

}
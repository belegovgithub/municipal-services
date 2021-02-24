package org.bel.birthdeath.birth.contract;
import org.bel.birthdeath.birth.model.EgBirthDtl;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BirthCertRequest {

	private String id;

	private String applicationno;

	private String createdby;

	private Long createdtime;
	
	private EgBirthDtl birthdtlid;

}
package org.bel.obm.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BankDetails {

	private String id; 
	private String accountHolderName;
	private String nameOfBank;
	private String accountNumber;
	private String accountType;
	private String ifscCode;
	private AuditDetails auditDetails = null;
}

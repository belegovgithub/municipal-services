
package org.bel.birthdeath.common.model;

import java.sql.Timestamp;

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
public class EmpDeclarationDtls {

	private String id;

	private String declaredby;

	private Timestamp declaredon;
	
	private Character agreed;

	private Timestamp startdate;

	private Timestamp enddate;
	
	private Character completed;
	
	private String startdateepoch;

	private String enddateepoch;
	
}

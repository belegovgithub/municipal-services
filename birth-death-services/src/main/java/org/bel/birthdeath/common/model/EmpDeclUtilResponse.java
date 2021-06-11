package org.bel.birthdeath.common.model;

import java.util.ArrayList;

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
public class EmpDeclUtilResponse {
	private ArrayList<DropDownData> years;
	private ArrayList<DropDownData> months;
}

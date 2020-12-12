package org.egov.pgr.contract;

import org.springframework.validation.annotation.Validated;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
public class LocalizationDto {
	
	private String code;
	
	private String message;
	
	private String module;
	
	private String locale;
	
	private String templateId;
	
}

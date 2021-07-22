package org.bel.obm.calculation;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.bel.obm.models.CHBookDtls;

import com.fasterxml.jackson.annotation.JsonProperty;

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
public class CalulationCriteria {
	@JsonProperty("chBookDtls")
	@Valid
	private CHBookDtls chBookDtls = null;

	@JsonProperty("applicationNumber")
	@Size(min = 2, max = 64)
	private String applicationNumber = null;

	@JsonProperty("tenantId")
	@NotNull
	@Size(min = 2, max = 256)
	private String tenantId = null;

}

package org.bel.obm.calculation;

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
public class Calculation {

	@JsonProperty("applicationNumber")
	private String applicationNumber = null;

	@JsonProperty("chBookDtls")
	private CHBookDtls chBookDtls = null;

	@NotNull
	@JsonProperty("tenantId")
	@Size(min = 2, max = 256)
	private String tenantId = null;

}

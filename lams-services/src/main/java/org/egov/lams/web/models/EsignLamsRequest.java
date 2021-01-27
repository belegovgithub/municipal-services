package org.egov.lams.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

import java.util.ArrayList;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.lams.models.pdfsign.LamsEsignDtls;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;



@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EsignLamsRequest   {
        @JsonProperty("RequestInfo")
        private RequestInfo requestInfo = null;

        @JsonProperty("LamsEsignDtls")
        @Valid
        private LamsEsignDtls lamsEsignDtls = null;
}


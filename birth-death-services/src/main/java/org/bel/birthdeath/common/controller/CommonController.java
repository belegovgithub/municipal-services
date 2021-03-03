package org.bel.birthdeath.common.controller;

import java.util.List;

import javax.validation.Valid;

import org.bel.birthdeath.birth.model.SearchCriteria;
import org.bel.birthdeath.common.contract.HospitalResponse;
import org.bel.birthdeath.common.contract.RequestInfoWrapper;
import org.bel.birthdeath.common.model.EgHospitalDtl;
import org.bel.birthdeath.common.services.CommonService;
import org.bel.birthdeath.utils.ResponseInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/common")
public class CommonController {
	
	@Autowired
	CommonService commonService;
	
	@Autowired
	private ResponseInfoFactory responseInfoFactory;
	
	@RequestMapping(value = { "/getHospitals"}, method = RequestMethod.POST)
    public ResponseEntity<HospitalResponse> search(@RequestBody RequestInfoWrapper requestInfoWrapper,
                                                       @Valid @ModelAttribute SearchCriteria criteria) {
        List<EgHospitalDtl> hospitalDtls = commonService.search(criteria.getTenantId());
        HospitalResponse response = HospitalResponse.builder().hospitalDtls(hospitalDtls).responseInfo(
                responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

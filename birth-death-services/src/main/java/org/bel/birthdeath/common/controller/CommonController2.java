package org.bel.birthdeath.common.controller;

import javax.validation.Valid;

import org.bel.birthdeath.birth.certmodel.BirthCertResponse;
import org.bel.birthdeath.birth.certmodel.BirthCertificate;
import org.bel.birthdeath.birth.model.SearchCriteria;
import org.bel.birthdeath.common.contract.RequestInfoWrapper;
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
public class CommonController2 {
	
	@Autowired
	CommonService commonService;
	
	@Autowired
	private ResponseInfoFactory responseInfoFactory;
	
	@RequestMapping(value = { "/getfilestoreid"}, method = RequestMethod.POST)
    public ResponseEntity<BirthCertResponse> download(@RequestBody RequestInfoWrapper requestInfoWrapper,
                                                       @Valid @ModelAttribute SearchCriteria criteria) {
		
        BirthCertificate birthCert = commonService.getBirthCertReqByConsumerCode(criteria,requestInfoWrapper.getRequestInfo());
        BirthCertResponse response = BirthCertResponse.builder().filestoreId(birthCert.getFilestoreid()).tenantId(criteria.getTenantId()).responseInfo(
                responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
	
}

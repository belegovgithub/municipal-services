package org.bel.birthdeath.birth.controller;

import java.util.List;

import javax.validation.Valid;

import org.bel.birthdeath.birth.certmodel.BirthCertRequest;
import org.bel.birthdeath.birth.certmodel.BirthCertResponse;
import org.bel.birthdeath.birth.certmodel.BirthCertificate;
import org.bel.birthdeath.birth.model.EgBirthDtl;
import org.bel.birthdeath.birth.model.SearchCriteria;
import org.bel.birthdeath.birth.service.BirthService;
import org.bel.birthdeath.common.contract.BirthResponse;
import org.bel.birthdeath.common.contract.RequestInfoWrapper;
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
@RequestMapping("/birth")
public class BirthController {
	
	@Autowired
	BirthService birthService;
	
	@Autowired
	private ResponseInfoFactory responseInfoFactory;
	
	@RequestMapping(value = { "/_search"}, method = RequestMethod.POST)
    public ResponseEntity<BirthResponse> search(@RequestBody RequestInfoWrapper requestInfoWrapper,
                                                       @Valid @ModelAttribute SearchCriteria criteria) {
        List<EgBirthDtl> birthCerts = birthService.search(criteria);
        BirthResponse response = BirthResponse.builder().birthCerts(birthCerts).responseInfo(
                responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
	
	@RequestMapping(value = { "/_download"}, method = RequestMethod.POST)
    public ResponseEntity<BirthCertResponse> download(@RequestBody RequestInfoWrapper requestInfoWrapper,
                                                       @Valid @ModelAttribute SearchCriteria criteria) {
		
        BirthCertificate birthCert = birthService.download(criteria,requestInfoWrapper.getRequestInfo());
        BirthCertResponse response ;
        if(birthCert.getCounter()<=0)
        	response = BirthCertResponse.builder().filestoreId(birthCert.getFilestoreid()).responseInfo(
                responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
                .build();
        else
        	response = BirthCertResponse.builder().consumerCode(birthCert.getBirthCertificateNo()).tenantId(birthCert.getTenantId())
        			.responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
                    .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

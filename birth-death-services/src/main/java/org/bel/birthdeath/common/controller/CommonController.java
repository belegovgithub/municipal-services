package org.bel.birthdeath.common.controller;

import java.util.List;

import javax.validation.Valid;

import org.bel.birthdeath.birth.model.EgBirthDtl;
import org.bel.birthdeath.birth.model.SearchCriteria;
import org.bel.birthdeath.common.contract.BirthResponse;
import org.bel.birthdeath.common.contract.DeathResponse;
import org.bel.birthdeath.common.contract.HospitalResponse;
import org.bel.birthdeath.common.contract.RequestInfoWrapper;
import org.bel.birthdeath.common.model.EgHospitalDtl;
import org.bel.birthdeath.common.services.CommonService;
import org.bel.birthdeath.death.model.EgDeathDtl;
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
	
	
	@RequestMapping(value = { "/saveBirthImport"}, method = RequestMethod.POST)
    public ResponseEntity<BirthResponse> saveBirthImport(@RequestBody RequestInfoWrapper requestInfoWrapper,
    		@RequestBody BirthResponse importJSon) {
        List<EgBirthDtl> egBirthDtls = commonService.saveBirthImport(importJSon,requestInfoWrapper.getRequestInfo());
        BirthResponse response = BirthResponse.builder().birthCerts(egBirthDtls).responseInfo(
                responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
	
	@RequestMapping(value = { "/saveDeathImport"}, method = RequestMethod.POST)
    public ResponseEntity<DeathResponse> saveDeathImport(@RequestBody RequestInfoWrapper requestInfoWrapper,
    		@RequestBody DeathResponse importJSon) {
        List<EgDeathDtl> egDeathDtls = commonService.saveDeathImport(importJSon,requestInfoWrapper.getRequestInfo());
        DeathResponse response = DeathResponse.builder().deathCerts(egDeathDtls).responseInfo(
                responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

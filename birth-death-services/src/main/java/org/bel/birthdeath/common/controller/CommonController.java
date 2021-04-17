package org.bel.birthdeath.common.controller;

import java.util.List;

import javax.validation.Valid;

import org.bel.birthdeath.birth.model.ImportBirthWrapper;
import org.bel.birthdeath.birth.model.SearchCriteria;
import org.bel.birthdeath.common.contract.BirthResponse;
import org.bel.birthdeath.common.contract.DeathResponse;
import org.bel.birthdeath.common.contract.HospitalResponse;
import org.bel.birthdeath.common.contract.RequestInfoWrapper;
import org.bel.birthdeath.common.model.EgHospitalDtl;
import org.bel.birthdeath.common.model.EmpDeclarationDtls;
import org.bel.birthdeath.common.services.CommonService;
import org.bel.birthdeath.death.model.ImportDeathWrapper;
import org.bel.birthdeath.utils.ResponseInfoFactory;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	
	@Value("${egov.bnd.excelimport.flag}")
    private boolean excelImportFlag;
	
	@Value("${egov.bnd.excelimport.tokenflag}")
    private boolean excelImportTokenFlag;
	
	@Value("${egov.bnd.excelimport.token}")
    private String excelImportToken;
	
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
    public ResponseEntity<ImportBirthWrapper> saveBirthImport(
    		@RequestBody BirthResponse importJSon) {
        ImportBirthWrapper importBirthWrapper = commonService.saveBirthImport(importJSon,importJSon.getRequestInfo());
        importBirthWrapper.setResponseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(importJSon.getRequestInfo(), true));
        return new ResponseEntity<>(importBirthWrapper, HttpStatus.OK);
    }
	
	@RequestMapping(value = { "/saveDeathImport"}, method = RequestMethod.POST)
    public ResponseEntity<ImportDeathWrapper> saveDeathImport(	@RequestBody DeathResponse importJSon) {
		ImportDeathWrapper importDeathWrapper = commonService.saveDeathImport(importJSon,importJSon.getRequestInfo());
		importDeathWrapper.setResponseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(importJSon.getRequestInfo(), true));
        return new ResponseEntity<>(importDeathWrapper, HttpStatus.OK);
    }
	
	@RequestMapping(value = { "/updateBirthImport"}, method = RequestMethod.POST)
    public ResponseEntity<ImportBirthWrapper> updateBirthImport(
    		@RequestBody BirthResponse importJSon) {
        ImportBirthWrapper importBirthWrapper = commonService.updateBirthImport(importJSon,importJSon.getRequestInfo());
        importBirthWrapper.setResponseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(importJSon.getRequestInfo(), true));
        return new ResponseEntity<>(importBirthWrapper, HttpStatus.OK);
    }
	
	@RequestMapping(value = { "/updateDeathImport"}, method = RequestMethod.POST)
    public ResponseEntity<ImportDeathWrapper> updateDeathImport(
    		@RequestBody DeathResponse importJSon) {
		ImportDeathWrapper importDeathWrapper = commonService.updateDeathImport(importJSon,importJSon.getRequestInfo());
		importDeathWrapper.setResponseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(importJSon.getRequestInfo(), true));
        return new ResponseEntity<>(importDeathWrapper, HttpStatus.OK);
    }
	
	@RequestMapping(value = { "/deleteBirthImport"}, method = RequestMethod.POST)
    public ResponseEntity<String>  deleteBirthImport(@RequestBody RequestInfoWrapper requestInfoWrapper,
            @ModelAttribute SearchCriteria criteria) {
		int deletedRecords=0;
        if(excelImportFlag && (excelImportTokenFlag ? excelImportToken.equals(criteria.getToken()) : true)) {	
        	deletedRecords = commonService.deleteBirthImport(criteria.getTenantId(),requestInfoWrapper.getRequestInfo());
        }
        return new ResponseEntity<>("Deleted Records : "+deletedRecords , HttpStatus.OK);
    }
	
	@RequestMapping(value = { "/deleteDeathImport"}, method = RequestMethod.POST)
	public ResponseEntity<String>  deleteDeathImport(@RequestBody RequestInfoWrapper requestInfoWrapper,
            @ModelAttribute SearchCriteria criteria) {
		int deletedRecords=0;
		if(excelImportFlag && (excelImportTokenFlag ? excelImportToken.equals(criteria.getToken()) : true)) {
			deletedRecords = commonService.deleteDeathImport(criteria.getTenantId(),requestInfoWrapper.getRequestInfo());
		}
        return new ResponseEntity<>("Deleted Records : "+deletedRecords , HttpStatus.OK);
    }
	
	@RequestMapping(value = { "/checkDeclaration" }, method = RequestMethod.POST)
	public ResponseEntity<EmpDeclarationDtls> checkDeclaration(@RequestBody RequestInfoWrapper requestInfoWrapper) {
		EmpDeclarationDtls declarationDtls;
		if(null!=requestInfoWrapper.getRequestInfo().getUserInfo().getUuid())
			declarationDtls = commonService.checkDeclaration(requestInfoWrapper.getRequestInfo().getUserInfo().getUuid());
		else
			throw new CustomException("INVALID_INPUT","UUID can not be empty.");
		return new ResponseEntity<>(declarationDtls, HttpStatus.OK);
	}

	@RequestMapping(value = { "/updateDeclaration" }, method = RequestMethod.POST)
	public ResponseEntity<String> updateDeclaration(@RequestBody RequestInfoWrapper requestInfoWrapper,
			@ModelAttribute EmpDeclarationDtls declarationDtls) {
		if(null!=requestInfoWrapper.getRequestInfo().getUserInfo().getUuid())
			declarationDtls.setDeclaredby(requestInfoWrapper.getRequestInfo().getUserInfo().getUuid());
		else
			throw new CustomException("INVALID_INPUT","UUID can not be empty.");
		return new ResponseEntity<>(commonService.updateDeclaration(declarationDtls), HttpStatus.OK);
	}
}

package org.egov.lams.web.controllers;

import org.egov.lams.models.pdfsign.LeasePdfApplicationRequest;
import org.egov.lams.models.pdfsign.PdfXmlResp;
import org.egov.lams.service.PdfSignService;
import org.egov.lams.web.models.RequestInfoWrapper;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/dSign")
public class LamsPdfSign {

	@Autowired
	private PdfSignService pdfSignService;

	@RequestMapping(value = { "/createApplicationPdf"}, method = RequestMethod.POST)
    public ResponseEntity<PdfXmlResp> createApplicationPdf(@RequestBody LeasePdfApplicationRequest leasePdfApplication) 
	{
        PdfXmlResp pdfXmlResp = pdfSignService.prepareRequest(leasePdfApplication);
        return new ResponseEntity<PdfXmlResp>(pdfXmlResp, HttpStatus.OK);
    }
	
	@PostMapping("/getApplicationfile")
	public ResponseEntity<String> create(@RequestBody RequestInfoWrapper requestInfoWrapper, @RequestParam("txnid") String txnid) {
		if(!pdfSignService.validateUser(requestInfoWrapper.getRequestInfo(),txnid))
		throw new CustomException("UNAUTHORIZED USER","Unauthorized user to access the application");
		String pdfXmlResp = pdfSignService.getApplicationfile(txnid);
		return new ResponseEntity<String>(pdfXmlResp, HttpStatus.OK);
	}
}

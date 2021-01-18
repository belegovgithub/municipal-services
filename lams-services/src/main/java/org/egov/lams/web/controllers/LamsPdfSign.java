package org.egov.lams.web.controllers;

import org.egov.lams.models.pdfsign.PdfXmlResp;
import org.egov.lams.service.PdfSignService;
import org.egov.lams.web.models.RequestInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/dSign")
public class LamsPdfSign {

	@Autowired
	private PdfSignService pdfSignService;

	@RequestMapping(value = { "/createApplicationPdf"}, method = RequestMethod.POST)
    public ResponseEntity<PdfXmlResp> createApplicationPdf() 
	{
        PdfXmlResp pdfXmlResp = pdfSignService.prepareRequest();
        return new ResponseEntity<PdfXmlResp>(pdfXmlResp, HttpStatus.OK);
    }
}

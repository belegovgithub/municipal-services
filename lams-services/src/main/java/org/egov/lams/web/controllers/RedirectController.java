package org.egov.lams.web.controllers;

import javax.servlet.http.HttpServletRequest;

import org.egov.lams.service.PdfSignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class RedirectController {

    @Value("${bel.default.lams.ui.url}")
    private String lamsUiUrl;

    @Value("${bel.ui.host}")
    private String uiHost;
    
	@Autowired
	private PdfSignService pdfSignService;

    @RequestMapping(value = "/lams/esignresp", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Object> esignresp(@RequestParam("eSignResponse") String eSignResponse,@RequestParam("espTxnID") String espTxnID,RedirectAttributes rdAttr, HttpServletRequest request) {
    	
    	request.setAttribute("txnid", espTxnID);
    	log.info(" Response--->"+eSignResponse+"ESP ID"+espTxnID);
    	String error = eSignResponse.substring(eSignResponse.indexOf("errCode"),eSignResponse.indexOf("resCode"));
    	
    	log.info("err occured " + error);
    	
    	boolean response = pdfSignService.prepareResponse(eSignResponse,espTxnID);
    	
        HttpHeaders httpHeaders = new HttpHeaders();
        String redirectUrl = uiHost + lamsUiUrl + "?espTxnID="+espTxnID+"&success="+response;
        httpHeaders.setLocation(UriComponentsBuilder.fromHttpUrl(redirectUrl)
                .build().toUri());
        log.info(httpHeaders!=null ? httpHeaders.toString(): "http header is null ");
        return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleError(Exception e) {
    	e.printStackTrace();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(UriComponentsBuilder.fromHttpUrl(uiHost + lamsUiUrl).build().encode().toUri());
        return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND);
    }


}

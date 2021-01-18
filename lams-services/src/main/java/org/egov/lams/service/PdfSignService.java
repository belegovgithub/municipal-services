package org.egov.lams.service;

import java.security.PrivateKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

import org.egov.lams.models.pdfsign.FormXmlDataAsp;
import org.egov.lams.models.pdfsign.PdfXmlResp;
import org.egov.lams.models.pdfsign.RequestXmlForm;
import org.egov.lams.util.PdfSignUtils;
import org.egov.lams.util.PdfSignXmlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PdfSignService {

	@Autowired
	private PdfSignUtils pdfSignUtils;
	
	@Autowired
	private PdfSignXmlUtils pdfSignXmlUtils;

    @Value("${bel.default.lams.resp.url}")
    private String lamsRespUrl;

    @Value("${bel.ui.host}")
    private String uiHost;
	
	public PdfXmlResp prepareRequest() {
		PdfXmlResp response = new PdfXmlResp();
		String fileHash = "";
		Random randNum = new Random();
		int randInt = randNum.nextInt();
		Calendar timenow  = Calendar.getInstance();
		String txnId = "" + timenow.getTimeInMillis() + "A" + randInt;
		txnId = txnId.replaceAll("-", "");
		log.info("txnid " + txnId);
		fileHash = pdfSignUtils.pdfSigner(txnId);
		Date now = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
		//try xml generation
		FormXmlDataAsp formXmalDataAsp = new FormXmlDataAsp();

		formXmalDataAsp.setVer("2.1");
		formXmalDataAsp.setSc("Y");
		formXmalDataAsp.setTs(dateFormat.format(now));
		//formXmalDataAsp.setTxn((myUploadForm.getAadhar() + randInt).replace("-", ""));


		formXmalDataAsp.setTxn(txnId);
		formXmalDataAsp.setEkycId("");
		formXmalDataAsp.setEkycIdType("A");
		formXmalDataAsp.setAspId("DGDE-900");
		formXmalDataAsp.setAuthMode("1");
		formXmalDataAsp.setResponseSigType("pkcs7");
		//formXmalDataAsp.setResponseUrl("url");
		formXmalDataAsp.setResponseUrl(uiHost + lamsRespUrl);
		formXmalDataAsp.setId("1");
		formXmalDataAsp.setHashAlgorithm("SHA256");
		formXmalDataAsp.setDocInfo("My Document");
		formXmalDataAsp.setDocHashHex(fileHash);

		//Get encrypted string/ signed data for xml signature tag
		String strToEncrypt = pdfSignXmlUtils.generateAspXml(formXmalDataAsp,txnId);
		System.out.println(" strToEncrypt " + strToEncrypt);
		String xmlData = "";
		try {
			PrivateKey rsaPrivateKey =  pdfSignUtils.getBase64PrivateKey();
			xmlData = pdfSignXmlUtils.signXmlString(strToEncrypt, rsaPrivateKey);
			log.info(xmlData);
		}
		catch(Exception e) {
			log.info("Error in Encryption.");
			e.printStackTrace();
			response.setError("error occurred in processing");
			return response;
		}

		RequestXmlForm myRequestXmlForm = new RequestXmlForm();
		myRequestXmlForm.setId("");
		myRequestXmlForm.setType("1");
		myRequestXmlForm.setDescription("Y");
		myRequestXmlForm.setESignRequest(xmlData);
		myRequestXmlForm.setAspTxnID(txnId);
		response.setDSignInfo(myRequestXmlForm);
		return response;
	}   

	public boolean prepareResponse(String eSignResponse, String espTxnID) {
		return pdfSignUtils.signPdfwithDS(eSignResponse, espTxnID);
	}

	public String getApplicationfile(String txnid) {
		return pdfSignUtils.signPdfwithDS(txnid);
	}
}


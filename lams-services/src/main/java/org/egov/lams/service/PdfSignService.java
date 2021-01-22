package org.egov.lams.service;

import java.security.PrivateKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import org.egov.lams.models.pdfsign.FormXmlDataAsp;
import org.egov.lams.models.pdfsign.LeasePdfApplicationRequest;
import org.egov.lams.models.pdfsign.PdfXmlResp;
import org.egov.lams.models.pdfsign.RequestXmlForm;
import org.egov.lams.util.PdfSignUtils;
import org.egov.lams.util.PdfSignXmlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;

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
    
	@Value("${bel.sign.egovpdf.host}")  
	private String egovpdfHost;

	@Value("${bel.sign.egovpdf.postendpoint}")  
	private String egovpdfPostendpoint;
	
	public PdfXmlResp prepareRequest(LeasePdfApplicationRequest leasePdfApplication) {
		PdfXmlResp response = new PdfXmlResp();
		if(null!=leasePdfApplication && !leasePdfApplication.getLeaseApplication().isEmpty())
		{
			String applResp =  getApplFile(leasePdfApplication);
			if(!leasePdfApplication.getLeaseApplication().get(0).isForEsign())
			{
				response.setFileStoreInfo(applResp);
				
			}
			else
			{
				Gson gson = new Gson();
				HashMap<String, Object> fileRespMap = (HashMap<String, Object>) gson.fromJson(applResp, HashMap.class);
				List<String> filestoreids = (List) fileRespMap.get("filestoreIds");
				log.info("filestoreids " + filestoreids.get(0));
				
				String fileHash = "";
				Random randNum = new Random();
				int randInt = randNum.nextInt();
				Calendar timenow  = Calendar.getInstance();
				String txnId = "" + timenow.getTimeInMillis() + "A" + randInt;
				txnId = txnId.replaceAll("-", "");
				log.info("txnid " + txnId);

				String filestoreId = filestoreids.get(0);
				fileHash = pdfSignUtils.pdfSigner(txnId,filestoreId);
				Date now = new Date();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
				//try xml generation
				FormXmlDataAsp formXmalDataAsp = new FormXmlDataAsp();
				formXmalDataAsp.setTxn(txnId);
				formXmalDataAsp.setResponseUrl(uiHost + lamsRespUrl);
				formXmalDataAsp.setDocInfo("My Document");
				formXmalDataAsp.setDocHashHex(fileHash);
				formXmalDataAsp.setTs(dateFormat.format(now));

				//Get encrypted string/ signed data for xml signature tag
				String strToEncrypt = pdfSignXmlUtils.generateAspXml(formXmalDataAsp,txnId);
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
			}
		}
		
		return response;
	}   

	public boolean prepareResponse(String eSignResponse, String espTxnID) {
		return pdfSignUtils.signPdfwithDS(eSignResponse, espTxnID);
	}

	public String getApplicationfile(String txnid) {
		return pdfSignUtils.getApplicationfile(txnid);
	}
	
	public String getApplFile(LeasePdfApplicationRequest leasePdfApplication)
	{
		try
		{
			RestTemplate restTemplate = new RestTemplate();
			String url = egovpdfHost + egovpdfPostendpoint;
			HttpMethod requestMethod = HttpMethod.POST;

			HttpEntity<LeasePdfApplicationRequest> requestEntity = new HttpEntity<LeasePdfApplicationRequest>(leasePdfApplication);

			ResponseEntity<String> response = restTemplate.exchange(url, requestMethod, requestEntity, String.class);

			log.info("file upload status code: " + response);
			if(response.getStatusCode().equals(HttpStatus.OK)) {
				return response.getBody().toString();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return "error";
	}
}


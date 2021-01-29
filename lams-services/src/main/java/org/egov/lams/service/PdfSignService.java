package org.egov.lams.service;

import java.security.PrivateKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.http.impl.client.HttpClients;
import org.egov.lams.model.SearchCriteria;
import org.egov.lams.models.pdfsign.EgovPdfResp;
import org.egov.lams.models.pdfsign.FormXmlDataAsp;
import org.egov.lams.models.pdfsign.LamsEsignDtls;
import org.egov.lams.models.pdfsign.LeasePdfApplication;
import org.egov.lams.models.pdfsign.LeasePdfApplicationRequest;
import org.egov.lams.models.pdfsign.PdfXmlResp;
import org.egov.lams.models.pdfsign.RequestXmlForm;
import org.egov.lams.repository.LamsRepository;
import org.egov.lams.util.CommonUtils;
import org.egov.lams.util.PdfSignUtils;
import org.egov.lams.util.PdfSignXmlUtils;
import org.egov.lams.web.models.AuditDetails;
import org.egov.lams.web.models.EsignLamsRequest;
import org.egov.lams.web.models.LeaseAgreementRenewalDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PdfSignService {

	@Autowired
	private PdfSignUtils pdfSignUtils;
	
	@Autowired
	private PdfSignXmlUtils pdfSignXmlUtils;
	
	@Autowired
	private LamsRepository repository;
	
	@Autowired
    private CommonUtils commUtils;

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
			EgovPdfResp egovPdfResp =  getApplFile(leasePdfApplication);
			if(!leasePdfApplication.getLeaseApplication().get(0).isForEsign())
			{
				response.setFileStoreInfo(egovPdfResp);
			}
			else
			{
				String fileHash = "";
				Random randNum = new Random();
				int randInt = randNum.nextInt();
				Calendar timenow  = Calendar.getInstance();
				String txnId = "" + timenow.getTimeInMillis() + "A" + randInt;
				txnId = txnId.replaceAll("-", "");
				log.info("txnid " + txnId);
				String filestoreId = egovPdfResp.getFilestoreIds().get(0);
				
				LamsEsignDtls lamsEsignDetals= new LamsEsignDtls();
				lamsEsignDetals.setId(UUID.randomUUID().toString());
				lamsEsignDetals.setTxnId(txnId);
				lamsEsignDetals.setFileStoreId(filestoreId);
				AuditDetails auditDetails = commUtils.getAuditDetails(leasePdfApplication.getRequestInfo().getUserInfo().getUuid(), true);
				lamsEsignDetals.setAuditDetails(auditDetails);
				lamsEsignDetals.setStatus("INITIATED");
				lamsEsignDetals.setSurveyId(leasePdfApplication.getLeaseApplication().get(0).getSurveyId());
				EsignLamsRequest esignRequest = new EsignLamsRequest();
				esignRequest.setLamsEsignDtls(lamsEsignDetals);
				repository.saveEsignDtls(esignRequest);
				
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
		return repository.getApplicationfile(txnid);
	}
	
	public EgovPdfResp getApplFile(LeasePdfApplicationRequest leasePdfApplication)
	{
		EgovPdfResp egovPdfResp = null;
		try
		{
			LeasePdfApplication temp = leasePdfApplication.getLeaseApplication().get(0);
			SearchCriteria criteria = new SearchCriteria();
			criteria.setSurveyId(temp.getSurveyId());
			List<LeaseAgreementRenewalDetail> leaseDtls = repository.getLeaseDetails(criteria);
			temp.setMobileNo(leasePdfApplication.getRequestInfo().getUserInfo().getMobileNumber());
			temp.setArea(leaseDtls.get(0).getArea());
			temp.setSurveyNo(leaseDtls.get(0).getSurveyNo());
			if(leaseDtls.get(0).getLocationId().equalsIgnoreCase("1")) {
				temp.setAddressee("The Chief Executive Officer");
				temp.setInstruction("within the civil area");
			}
			else if(leaseDtls.get(0).getLocationId().equalsIgnoreCase("2")) {
				temp.setAddressee("The Defence Estates Officer");
				temp.setInstruction("outside civil area");
			}
			RestTemplate restTemplate = new RestTemplate();
			MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
			mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_PDF, MediaType.APPLICATION_OCTET_STREAM));
			restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);
			String url = egovpdfHost + egovpdfPostendpoint;
			HttpMethod requestMethod = HttpMethod.POST;

			HttpEntity<LeasePdfApplicationRequest> requestEntity = new HttpEntity<LeasePdfApplicationRequest>(leasePdfApplication);

			ResponseEntity<EgovPdfResp> response = restTemplate.exchange(url, requestMethod, requestEntity, EgovPdfResp.class);

			log.info("file upload status code: " + response);
			if(response.getStatusCode().equals(HttpStatus.OK)) {
				return response.getBody();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return egovPdfResp;
	}
}


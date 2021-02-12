package org.egov.lams.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSigProperties;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSignDesigner;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.util.Hex;
import org.egov.lams.models.pdfsign.LamsEsignDtls;
import org.egov.lams.repository.LamsRepository;
import org.egov.lams.web.models.AuditDetails;
import org.egov.lams.web.models.EsignLamsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PdfSignUtils {

	@Value("${bel.sign.publickey}")  
	private String pubKey;

	@Value("${bel.sign.privatekey}")  
	private String privKey;

	@Value("${bel.sign.filestore.host}")  
	private String filestoreHost;

	@Value("${bel.sign.filestore.getendpoint}")  
	private String filestoreGetendpoint;

	@Value("${bel.sign.filestore.postendpoint}")  
	private String filestorePostendpoint;

	@Value("${bel.session.max.time.diffinmilli:300000l}")  
	private Long esignMaxTimeMilli;

	@Autowired
	private PdfSignXmlUtils pdfSignXmlUtils;
	
	@Autowired
	private LamsRepository repository;

	private static Map<String, Path> signedTempFileMap = new HashMap<String, Path>();
	public static Map<String, Integer> offsetMap = new HashMap<String, Integer>();
	
	private PrivateKey privateKey;

	private PublicKey publicKey;
	

	public String pdfSigner(String txnid , String fileStoreId) {

		String hashDocument = null;
		Path tempFile = null;
		Path signedTempFile = null;
		try {
			tempFile = Files.createTempFile("esign", ".pdf");
			signedTempFile = Files.createTempFile("esigned", ".pdf");
			String url = filestoreHost +  filestoreGetendpoint + "?fileStoreId="+fileStoreId+"&tenantId=pb";
			RestTemplate restTemplate = new RestTemplate();
			byte[] pdfBytes = restTemplate.getForObject(url, byte[].class);

			Files.write(tempFile, pdfBytes);

			File documentFile = new File(tempFile.toString());
    		InputStream imageStream = getClass().getClassLoader().getResourceAsStream("profile.png");

    		String name = documentFile.getName();

    		// page is 1-based here
    		int page = 1;
    		PDVisibleSignDesigner visibleSignDesigner = new PDVisibleSignDesigner(tempFile.toString(), imageStream, page);
    		PDVisibleSigProperties visibleSignatureProperties = new PDVisibleSigProperties();
    		visibleSignDesigner.xAxis(470).yAxis(490).zoom(-60).adjustForRotation();
    		imageStream.close();
    		visibleSignatureProperties.signerName(name).signerLocation("location").signatureReason("Security").
    		preferredSize(0).page(1).visualSignEnabled(true).
    		setPdVisibleSignature(visibleSignDesigner);

    		// load document
    		PDDocument doc = PDDocument.load(documentFile);

    		int accessPermissions = SigUtils.getMDPPermission(doc);
    		if (accessPermissions == 1)
    		{
    			throw new IllegalStateException("No changes to the document are permitted due to DocMDP transform parameters dictionary");
    		}
    		// Note that PDFBox has a bug that visual signing on certified files with permission 2
    		// doesn't work properly, see PDFBOX-3699. As long as this issue is open, you may want to 
    		// be careful with such files.        

    		PDSignature signature  = new PDSignature();

    		// Optional: certify
    		// can be done only if version is at least 1.5 and if not already set
    		// doing this on a PDF/A-1b file fails validation by Adobe preflight (PDFBOX-3821)
    		// PDF/A-1b requires PDF version 1.4 max, so don't increase the version on such files.
    		if (doc.getVersion() >= 1.5f && accessPermissions == 0)
    		{
    			SigUtils.setMDPPermission(doc, signature, 2);
    		}

    		PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
    		if (acroForm != null && acroForm.getNeedAppearances())
    		{
    			// PDFBOX-3738 NeedAppearances true results in visible signature becoming invisible 
    			// with Adobe Reader
    			if (acroForm.getFields().isEmpty())
    			{
    				// we can safely delete it if there are no fields
    				acroForm.getCOSObject().removeItem(COSName.NEED_APPEARANCES);
    				// note that if you've set MDP permissions, the removal of this item
    				// may result in Adobe Reader claiming that the document has been changed.
    				// and/or that field content won't be displayed properly.
    				// ==> decide what you prefer and adjust your code accordingly.
    			}
    			else
    			{
    				log.info("/NeedAppearances is set, signature may be ignored by Adobe Reader");
    			}
    		}

    		// default filter
    		signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);

    		// subfilter for basic and PAdES Part 2 signatures
    		signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);

    		if (visibleSignatureProperties != null)
    		{
    			// this builds the signature structures in a separate document
    			visibleSignatureProperties.buildSignature();

    			signature.setName(visibleSignatureProperties.getSignerName());
    			signature.setLocation(visibleSignatureProperties.getSignerLocation());
    			signature.setReason(visibleSignatureProperties.getSignatureReason());
    		}

    		// the signing date, needed for valid signature
    		signature.setSignDate(Calendar.getInstance());

    		SignatureOptions signatureOptions = new SignatureOptions();
    		signatureOptions.setVisualSignature(visibleSignatureProperties.getVisibleSignature());
    		signatureOptions.setPage(visibleSignatureProperties.getPage() - 1);
    		doc.addSignature(signature, null, signatureOptions);

    		FileOutputStream fos = new FileOutputStream(signedTempFile.toFile());
    		ExternalSigningSupport externalSigning = doc.saveIncrementalForExternalSigning(fos);

    		InputStream is = externalSigning.getContent();
			hashDocument = DigestUtils.sha256Hex(is);
			
            externalSigning.setSignature(new byte[0]);
            int offset = signature.getByteRange()[1] + 1;
            doc.close();
            IOUtils.closeQuietly(signatureOptions);
            
            signedTempFileMap.put(String.valueOf(txnid), signedTempFile);
            offsetMap.put(String.valueOf(txnid), offset);
			
		} catch (Exception e) {
			e.printStackTrace();
			log.info("Error in signing doc.");
		}
		finally {
			try {
				Files.deleteIfExists(tempFile);
			} catch (IOException e) {
				log.error("temp file deletion failed");
			}
		}
		return hashDocument;
	}

	public void checkandupdatemap() {
		try {
			log.info("b4 signedTempFileMap size " + signedTempFileMap.keySet().size());
			log.info("b4 offsetMap size " + offsetMap.keySet().size());
			Calendar now = Calendar.getInstance();
			long max = now.getTimeInMillis() - esignMaxTimeMilli;
			signedTempFileMap.entrySet().removeIf(entry -> {
				try {
					long time = Long.valueOf(entry.getKey().split("A")[0]);
					if(time < max)
					{
						if(offsetMap.containsKey(entry.getKey()))
						{
							offsetMap.remove(entry.getKey());
						}
						Files.deleteIfExists(signedTempFileMap.get(entry.getKey()));
						return true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			});
			log.info("after signedTempFileMap size " + signedTempFileMap.keySet().size());
			log.info("after offsetMap size " + offsetMap.keySet().size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean signPdfwithDS(String response, String txnid) {
		EsignLamsRequest esignLamsRequest = new EsignLamsRequest();
		LamsEsignDtls esignDtls = new LamsEsignDtls();
		esignDtls.setTxnId(txnid);
		esignDtls.setAuditDetails(AuditDetails.builder().lastModifiedTime(System.currentTimeMillis()).build());
		if(!signedTempFileMap.containsKey(txnid) || !offsetMap.containsKey(txnid))
		{
			try
			{
				if(signedTempFileMap.containsKey(txnid))
				{
					Files.deleteIfExists(signedTempFileMap.get(txnid));
					signedTempFileMap.remove(txnid);
				}
				offsetMap.remove(txnid);
				//record in db as error happened with errorCode as probable duplicate call
				esignDtls.setStatus("FAILED");
				esignDtls.setErrorCode("DUPLICATE CALL");
				esignLamsRequest.setLamsEsignDtls(esignDtls);
				updateEsignDetails(esignLamsRequest);
			}catch (Exception e) {
				e.printStackTrace();
				//record in db as error happened in processing
				esignDtls.setStatus("FAILED");
				esignDtls.setErrorCode("PROCESSING ERROR INIT");
				esignLamsRequest.setLamsEsignDtls(esignDtls);
				updateEsignDetails(esignLamsRequest);
				
			}
			return false;
		}
		Document xmlDoc = pdfSignXmlUtils.parseXmlString(response);
		if(null!=xmlDoc && pdfSignXmlUtils.verifySignature(xmlDoc, publicKey))
		{
			log.info("verify signature succeeded");
			try {
				String errorCode = pdfSignXmlUtils.getErrorCode(xmlDoc);
				errorCode = errorCode.trim();
				if ("NA".equalsIgnoreCase(errorCode)) 
				{
					String pkcsResponse = pdfSignXmlUtils.getSignatureStr(xmlDoc);
					byte[] cmsSignature =  Base64.decodeBase64(pkcsResponse);
					
	                RandomAccessFile raf = new RandomAccessFile(signedTempFileMap.get(txnid).toFile(), "rw");
	                raf.seek(offsetMap.get(txnid));
	                raf.write(Hex.getBytes(cmsSignature));
	                raf.close();
		    		
					String fileStoreId=null;
					fileStoreId = uploadFile(txnid);
					if(null!= fileStoreId) {
					//record in db as success with filestoreid
					esignDtls.setFileStoreId(fileStoreId);
					esignDtls.setStatus("SUCCESS");
					esignLamsRequest.setLamsEsignDtls(esignDtls);
					updateEsignDetails(esignLamsRequest);
					return true;
					}
				}
				else
				{
					log.error("esign error occured " + errorCode);
					//record in db as error happened with errorCode
					esignDtls.setStatus("FAILED");
					esignDtls.setErrorCode(errorCode);
					esignLamsRequest.setLamsEsignDtls(esignDtls);
					updateEsignDetails(esignLamsRequest);
				}
			} catch (Exception e) {
				e.printStackTrace();
				//record in db as error happened in processing
				esignDtls.setStatus("FAILED");
				esignDtls.setErrorCode("PROCESSING ERROR");
				esignLamsRequest.setLamsEsignDtls(esignDtls);
				updateEsignDetails(esignLamsRequest);
			}
		}
		else
		{
			log.info("verify signature failed");
			//record in db as error happened with signature verification failed
			esignDtls.setStatus("FAILED");
			esignDtls.setErrorCode("SIGNATURE VERIFICATION FAILED");
			esignLamsRequest.setLamsEsignDtls(esignDtls);
			updateEsignDetails(esignLamsRequest);
		}
		try
		{
			if(signedTempFileMap.containsKey(txnid))
			{
				Files.deleteIfExists(signedTempFileMap.get(txnid));
				signedTempFileMap.remove(txnid);
			}
			offsetMap.remove(txnid);
		}catch (Exception e) {
			esignDtls.setStatus("FAILED");
			esignDtls.setErrorCode("PROCESSING ERROR");
			esignLamsRequest.setLamsEsignDtls(esignDtls);
			updateEsignDetails(esignLamsRequest);
			return false;
		}
		checkandupdatemap();
		return false;
	}

	public String uploadFile(String txnid) {
		try {
			RestTemplate restTemplate = new RestTemplate();
			String url = filestoreHost + filestorePostendpoint;
			HttpMethod requestMethod = HttpMethod.POST;

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);

			log.info("Creating and Uploading Test File: " + signedTempFileMap.get(txnid));

			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("file",  new FileSystemResource(signedTempFileMap.get(txnid).toFile()));
			body.add("tenantId", "pb");
			body.add("module", "lams-esign");

			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

			ResponseEntity<String> response = restTemplate.exchange(url, requestMethod, requestEntity, String.class);

			log.info("file upload status code: " + response);
			if(response.getStatusCode().equals(HttpStatus.CREATED)) {
				String fileStoreId= JsonPath.read(response.getBody(), "$.files[0].fileStoreId");
				log.info("uploaded fileStoreId "+fileStoreId);
				try {
					Files.deleteIfExists(signedTempFileMap.get(txnid));
				} catch (IOException e) {
					log.error("temp file deletion failed");
				}
				return fileStoreId;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				Files.deleteIfExists(signedTempFileMap.get(txnid));
			} catch (IOException e) {
				log.error("temp file deletion failed");
			}
		}
		return null;
	}

	public PrivateKey getBase64PrivateKey() 
	{
		return privateKey;
	}

	@PostConstruct
	private void postConstruct() {
		try
		{
			byte[] keyBytes =  java.util.Base64.getDecoder().decode(privKey);

			PKCS8EncodedKeySpec spec =
					new PKCS8EncodedKeySpec(keyBytes);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			privateKey =  kf.generatePrivate(spec);
		}catch (Exception e) {
			log.error("failed to load private key");
		}
		try
		{
			CertificateFactory f = CertificateFactory.getInstance("X.509");
			X509Certificate certificate = (X509Certificate) f
					.generateCertificate(new ByteArrayInputStream(java.util.Base64.getDecoder().decode(pubKey)));
			publicKey = certificate.getPublicKey();
		}catch (Exception e) {
			log.error("failed to load public key");
		}
	}

	private void updateEsignDetails(EsignLamsRequest esignRequest) {
		repository.updateEsignDtls(esignRequest);
	}
}


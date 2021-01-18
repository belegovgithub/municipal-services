package org.egov.lams.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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

import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.itextpdf.text.Font;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfDate;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignature;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfSignatureAppearance.RenderingMode;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfString;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PdfSignUtils {
	
	@Value("${bel.sign.publickey}")  
	private String publicKey;

	@Value("${bel.sign.privatekey}")  
	private String privKey;

	@Value("${bel.sign.filestore.host}")  
	private String filestoreHost;
	
	@Value("${bel.sign.filestore.getendpoint}")  
	private String filestoreGetendpoint;

	@Value("${bel.sign.filestore.postendpoint}")  
	private String filestorePostendpoint;
	
	public static Map<String, PdfSignatureAppearance> appearanceTxnMap = new HashMap<String, PdfSignatureAppearance>();
	
	public static Map<String, ByteArrayOutputStream> byteArrayOutputStreamMap = new HashMap<String, ByteArrayOutputStream>();

	@Autowired
	private PdfSignXmlUtils pdfSignXmlUtils;
	
	public String pdfSigner(String txnid) {

		String hashDocument = null;
		PdfReader reader;
		try {

			String url = filestoreHost +  filestoreGetendpoint + "?fileStoreId=2a0f410b-1c19-4a2a-bcaf-f50927874ddb&tenantId=pb";
			System.out.println("url " + url);
			RestTemplate restTemplate = new RestTemplate();
			byte[] imageBytes = restTemplate.getForObject(url, byte[].class);
			Path testFile = Files.createTempFile("test-file", ".pdf");
			Files.write(testFile, imageBytes);
			
			String sourcefile = testFile.toString();
			log.info("Path--->" + sourcefile);
//			String destFile = sourcefile.replace(file.getName(), "Esigned" + request.getAttribute("txnid") + ".pdf");
			// destFile=sourcefile.replace(file.getName(), "/Signed_Pdf.pdf");
			// request.getSession().setAttribute("fileName","/Signed_Pdf.pdf");
			reader = new PdfReader(sourcefile);

			Rectangle cropBox = reader.getCropBox(1);
			Rectangle rectangle = null;
			String user = null;
			rectangle = new Rectangle(cropBox.getLeft(), cropBox.getBottom(), cropBox.getLeft(100),
					cropBox.getBottom(90));
//			FileOutputStream fout = new FileOutputStream(destFile);
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			PdfStamper stamper = PdfStamper.createSignature(reader, byteArrayOutputStream, '\0', null, true);

			PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
			appearance.setRenderingMode(RenderingMode.DESCRIPTION);
			appearance.setAcro6Layers(false);
			Font font = new Font();
			font.setSize(6);
			font.setFamily("Helvetica");
			font.setStyle("italic");
			appearance.setLayer2Font(font);
			Calendar currentDat = Calendar.getInstance();
			currentDat.add(currentDat.MINUTE, 5);
			appearance.setSignDate(currentDat);

			if (user == null || user == "null" || user.equals(null) || user.equals("null")) {
				appearance.setLayer2Text("Signed");
			} else {
				appearance.setLayer2Text("Signed by " + user);
			}
			appearance.setCertificationLevel(PdfSignatureAppearance.NOT_CERTIFIED);
			appearance.setImage(null);
			appearance.setVisibleSignature(rectangle, reader.getNumberOfPages(), null);

			int contentEstimated = 8192;
			HashMap<PdfName, Integer> exc = new HashMap();
			exc.put(PdfName.CONTENTS, contentEstimated * 2 + 2);

			PdfSignature dic = new PdfSignature(PdfName.ADOBE_PPKLITE, PdfName.ADBE_PKCS7_DETACHED);
			dic.setReason(appearance.getReason());
			dic.setLocation(appearance.getLocation());
			dic.setDate(new PdfDate(appearance.getSignDate()));

			appearance.setCryptoDictionary(dic);
			appearance.preClose(exc);
			checkandupdatemap();
			appearanceTxnMap.put(txnid, appearance);
			byteArrayOutputStreamMap.put(txnid, byteArrayOutputStream);

			InputStream is = appearance.getRangeStream();

			hashDocument = DigestUtils.sha256Hex(is);
			log.info("hex:    " + is.toString());
			Files.delete(testFile);
		} catch (Exception e) {
			e.printStackTrace();
			log.info("Error in signing doc.");

		}
		return hashDocument;
	}

	private void checkandupdatemap() {
		try {
			log.info("size b4 " + appearanceTxnMap.keySet().size());
			Calendar now = Calendar.getInstance();
			long max = now.getTimeInMillis() - 300000l;
			appearanceTxnMap.entrySet().removeIf(entry -> {
				long time = Long.valueOf(entry.getKey().split("A")[0]);
				return (time < max ? true : false);
			});
			log.info("size after " + appearanceTxnMap.keySet().size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void signPdfwithDS(String response, String txnid) {
		if(verifySignature(response))
		{
			log.info("verify signature succeeded");
			int contentEstimated = 8192;
			try {
				String errorCode = response.substring(response.indexOf("errCode"), response.indexOf("errMsg"));
				errorCode = errorCode.trim();
				if (errorCode.contains("NA")) {
					String pkcsResponse = pdfSignXmlUtils.parseXml(response.trim());
					byte[] sigbytes = Base64.decodeBase64(pkcsResponse);
					byte[] paddedSig = new byte[contentEstimated];
					System.arraycopy(sigbytes, 0, paddedSig, 0, sigbytes.length);
					PdfDictionary dic2 = new PdfDictionary();
					dic2.put(PdfName.CONTENTS, new PdfString(paddedSig).setHexWriting(true));
					// fout.close();
					PdfSignatureAppearance appearance1 = appearanceTxnMap.get(txnid);

					appearance1.close(dic2);

					ByteArrayOutputStream byteArrayOutputStream =
							byteArrayOutputStreamMap.get(txnid);
					uploadFile(byteArrayOutputStream);
					byteArrayOutputStream.flush();
					byteArrayOutputStream.close();
					byteArrayOutputStreamMap.remove(txnid);
					checkandupdatemap();
				} 
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else
		{
			log.info("verify signature failed");
		}
	}
	
	public void uploadFile(ByteArrayOutputStream byteArrayOutputStream) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = filestoreHost + filestorePostendpoint;
            HttpMethod requestMethod = HttpMethod.POST;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            log.info("byte arr size " + byteArrayOutputStream.toByteArray().length);
            HttpEntity<byte[]> fileEntity = new HttpEntity<>(byteArrayOutputStream.toByteArray());
            
            Path testFile = Files.createTempFile("test-file", ".pdf");
            log.info("Creating and Uploading Test File: " + testFile);
            Files.write(testFile,byteArrayOutputStream.toByteArray());
           

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file",  new FileSystemResource(testFile.toFile()));
            body.add("tenantId", "pb");
            body.add("module", "rainmaker-pgr");
            

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, requestMethod, requestEntity, String.class);
            
            Files.delete(testFile);

            log.info("file upload status code: " + response);

        } catch (Exception e) {
            e.printStackTrace();
        }

}

	private boolean verifySignature(String response) {
		try {
			log.info("verify sig ");

			CertificateFactory f = CertificateFactory.getInstance("X.509");
			X509Certificate certificate = (X509Certificate) f
					.generateCertificate(new ByteArrayInputStream(java.util.Base64.getDecoder().decode(publicKey)));
			PublicKey pk = certificate.getPublicKey();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(response.getBytes()));

			// Find Signature element
			NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
			if (nl.getLength() == 0) {
				throw new Exception("Cannot find Signature element");
			}

			// Create a DOM XMLSignatureFactory that will be used to unmarshal the
			// document containing the XMLSignature
			XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

			// Create a DOMValidateContext and specify a KeyValue KeySelector
			// and document context
			DOMValidateContext valContext = new DOMValidateContext(pk, nl.item(0));

			log.info("val " + nl.item(0).toString());
			// unmarshal the XMLSignature
			XMLSignature signature = fac.unmarshalXMLSignature(valContext);
			log.info("val " + signature);
			// Validate the XMLSignature (generated above)
			return (signature.validate(valContext));

		} catch (Exception e) {
			e.printStackTrace();

		}
		return false;
	}
	
	public PrivateKey getBase64PrivateKey()
			throws Exception {

		byte[] keyBytes =  java.util.Base64.getDecoder().decode(privKey);

		PKCS8EncodedKeySpec spec =
				new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePrivate(spec);
	}

}


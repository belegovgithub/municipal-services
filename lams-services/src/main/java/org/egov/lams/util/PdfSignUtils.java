package org.egov.lams.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;
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

import lombok.extern.slf4j.Slf4j;

import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfString;

@Component
@Slf4j
public class PdfSignUtils {
	String    destFile=null;
	// HttpSession session = null;
	FileOutputStream fout;

	PdfSignatureAppearance appearance;

	public static Map<String,PdfSignatureAppearance> appearanceTxnMap = new HashMap<String, PdfSignatureAppearance>();

	public String pdfSigner(File file, HttpServletRequest request, HttpSession session) {


		String hashDocument =null;
		PdfReader reader;
		try {
			String sourcefile=file.getAbsolutePath();
			System.out.println("Path--->"+sourcefile);
			destFile=sourcefile.replace(file.getName(), "Esigned"+ request.getAttribute("txnid") + ".pdf");
			//     destFile=sourcefile.replace(file.getName(), "/Signed_Pdf.pdf");
			//     request.getSession().setAttribute("fileName","/Signed_Pdf.pdf");
			reader =  new PdfReader(sourcefile);

			Rectangle cropBox = reader.getCropBox(1);
			Rectangle rectangle = null;
			String user=null;
			rectangle = new Rectangle(cropBox.getLeft(),cropBox.getBottom(), cropBox.getLeft(100),cropBox.getBottom(90));
			fout = new FileOutputStream(destFile);
			PdfStamper stamper = PdfStamper.createSignature(reader, fout, '\0', null, true);

			appearance= stamper.getSignatureAppearance();
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

			if(user == null || user == "null" || user.equals(null) || user.equals("null") ){
				appearance.setLayer2Text("Signed");
			}else{
				appearance.setLayer2Text("Signed by "+user);
			}
			appearance.setCertificationLevel(PdfSignatureAppearance.NOT_CERTIFIED);
			appearance.setImage(null);
			appearance.setVisibleSignature(rectangle,
					reader.getNumberOfPages(), null);

			int contentEstimated = 8192;
			HashMap<PdfName, Integer> exc = new HashMap();
			exc.put(PdfName.CONTENTS, contentEstimated * 2 + 2);

			PdfSignature dic = new PdfSignature(PdfName.ADOBE_PPKLITE,
					PdfName.ADBE_PKCS7_DETACHED);
			dic.setReason(appearance.getReason());
			dic.setLocation(appearance.getLocation());
			dic.setDate(new PdfDate(appearance.getSignDate()));

			appearance.setCryptoDictionary(dic);
			//  request.getSession().setAttribute("pdfHash",appearance);
			appearance.preClose(exc);
			// fout.close();
			checkandupdatemap();
			appearanceTxnMap.put(String.valueOf(request.getAttribute("txnid")), appearance);
			request.getSession().setAttribute("appearance",appearance);
			// System.gc();
			// getting bytes of file
			InputStream is = appearance.getRangeStream();

			hashDocument = DigestUtils.sha256Hex(is);
			//session=request.getSession();
			//session.setAttribute("appearance1",appearance);
			System.out.println("hex:    " + is.toString());
		} catch (Exception e) {
			System.out.println("Error in signing doc.");

		}
		return hashDocument;

	}

	private void checkandupdatemap() {
		try
		{
			System.out.println("size b4 " + appearanceTxnMap.keySet().size());
			Calendar now = Calendar.getInstance();
			long max = now.getTimeInMillis() - 300000l;
			for (String string : appearanceTxnMap.keySet()) {
				long time = Long.valueOf(string.split("A")[0]);
				if(time <  max )
				{
					appearanceTxnMap.remove(string);
				}
			}
			System.out.println("size after " + appearanceTxnMap.keySet().size());
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String signPdfwithDS(String response,HttpServletRequest request, HttpSession session) {
		session = request.getSession(false);
		//PdfSignatureAppearance appearance = (PdfSignatureAppearance)request.getSession().getAttribute("appearance");
		verifySignature(response);   
		int contentEstimated = 8192; 
		try {
			if(request.getSession() == null) {
				System.out.println("=================session===========");
			}
			//   PdfSignatureAppearance appearance = (PdfSignatureAppearance)request.getSession().getAttribute("pdfHash");
			//String esignRespResult = DocSignature;
			String errorCode = response.substring(response.indexOf("errCode"),response.indexOf("errMsg"));
			errorCode = errorCode.trim();
			if(errorCode.contains("NA")) {
				String pkcsResponse = new PdfSignXmlUtils().parseXml(response.trim());
				byte[] sigbytes = Base64.decodeBase64(pkcsResponse);
				byte[] paddedSig = new byte[contentEstimated];
				System.arraycopy(sigbytes, 0, paddedSig, 0, sigbytes.length);
				PdfDictionary dic2 = new PdfDictionary();
				dic2.put(PdfName.CONTENTS,
						new PdfString(paddedSig).setHexWriting(true));
				//fout.close();
				String key = String.valueOf(request.getAttribute("txnid"));
				PdfSignatureAppearance appearance1 = appearanceTxnMap.get(key);
				appearance1.close(dic2); 
				checkandupdatemap();
			}
			else {
				destFile = "Error";
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return destFile;
	}

	private void verifySignature(String response)
	{
		try
		{
			System.out.println("verify sig ");
			// Instantiate the document to be validated
			FileInputStream fin = new FileInputStream("D:\\pai\\egov\\cdacesign\\e-sign\\es-staging.txt");

			CertificateFactory f = CertificateFactory.getInstance("X.509");
			X509Certificate certificate = (X509Certificate)f.generateCertificate(fin);
			PublicKey pk = certificate.getPublicKey();		
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			Document doc =
					dbf.newDocumentBuilder().parse(new ByteArrayInputStream(response.getBytes()));

			// Find Signature element
			NodeList nl =
					doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
			if (nl.getLength() == 0) {
				throw new Exception("Cannot find Signature element");
			}

			// Create a DOM XMLSignatureFactory that will be used to unmarshal the
			// document containing the XMLSignature
			XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

			// Create a DOMValidateContext and specify a KeyValue KeySelector
			// and document context
			DOMValidateContext valContext = new DOMValidateContext
					(pk, nl.item(0));

			System.out.println("val " + nl.item(0).toString());
			// unmarshal the XMLSignature
			XMLSignature signature = fac.unmarshalXMLSignature(valContext);
			System.out.println("val " + signature);
			// Validate the XMLSignature (generated above)
			boolean coreValidity = signature.validate(valContext);

			// Check core validation status
			if (coreValidity == false) {
				System.err.println("Signature failed core validation");
				boolean sv = signature.getSignatureValue().validate(valContext);
				System.out.println("signature validation status: " + sv);
				// check the validation status of each Reference
				Iterator i = signature.getSignedInfo().getReferences().iterator();
				for (int j=0; i.hasNext(); j++) {
					boolean refValid =
							((Reference) i.next()).validate(valContext);
					System.out.println("ref["+j+"] validity status: " + refValid);
				}
			} else {
				System.out.println("Signature passed core validation");
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static PrivateKey getPrivateKey(String filename)
			throws Exception {

		byte[] keyBytes = Files.readAllBytes(Paths.get(filename));

		PKCS8EncodedKeySpec spec =
				new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePrivate(spec);
	}

}


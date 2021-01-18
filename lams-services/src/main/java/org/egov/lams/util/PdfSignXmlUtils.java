package org.egov.lams.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.PrivateKey;
import java.util.Collections;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.egov.lams.models.pdfsign.FormXmlDataAsp;
import org.springframework.stereotype.Component;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class PdfSignXmlUtils {

    public String signXmlString(String xmlIn, PrivateKey privateKey)throws Exception {
    	// Create a DOM XMLSignatureFactory that will be used to
    	// generate the enveloped signature.
    	XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

    	// Create a Reference to the enveloped document (in this case,
    	// you are signing the whole document, so a URI of "" signifies
    	// that, and also specify the SHA1 digest algorithm and
    	// the ENVELOPED Transform.
    	Reference ref = fac.newReference("", fac.newDigestMethod(DigestMethod.SHA256,
    	null),
    	Collections.singletonList(fac.newTransform(Transform.ENVELOPED,
    	(TransformParameterSpec) null)),
    	null, null);

    	// Create the SignedInfo.
    	SignedInfo si =
    	fac.newSignedInfo(fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE,
    	(C14NMethodParameterSpec) null),
    	fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
    	Collections.singletonList(ref));

    	// Instantiate the document to be signed.
    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	dbf.setNamespaceAware(true);
    	
    	Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xmlIn)));
    	
    	// Create a DOMSignContext and specify the RSA PrivateKey and
    	// location of the resulting XMLSignature's parent element.
    	DOMSignContext dsc = new DOMSignContext(privateKey, doc.getDocumentElement());

    	// Create the XMLSignature, but don't sign it yet.
    	XMLSignature signature = fac.newXMLSignature(si, null);

    	// Marshal, generate, and sign the enveloped signature.
    	signature.sign(dsc);

    	// Output the resulting document.
    	OutputStream os = new ByteArrayOutputStream();
    	TransformerFactory tf = TransformerFactory.newInstance();
    	Transformer trans = tf.newTransformer();

    	trans.transform(new DOMSource(doc), new StreamResult(os));

    	return os.toString();
      }
    
    public String parseXml(String esignResponse)throws Exception {
    
    	DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(esignResponse));

        Document doc = db.parse(is);
    	//Document doc = getXmlDocument(esignResponse);
    	NodeList node = doc.getElementsByTagName("DocSignature");
    	String sig = node.item(0).getTextContent();
    	return sig;  
    }
    
    public String generateAspXml(FormXmlDataAsp aspXmlDetais, String txnId) {
    	try {

    		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

    		// root elements
    		Document doc = docBuilder.newDocument();
    		Element esign = doc.createElement("Esign");
    		doc.appendChild(esign);

    		// set attribute to esign Esign
    		Attr attr = doc.createAttribute("ver");
    		attr.setValue(aspXmlDetais.getVer());
    		esign.setAttributeNode(attr);

    		attr = doc.createAttribute("sc");
    		attr.setValue(aspXmlDetais.getSc());
    		esign.setAttributeNode(attr);

    		attr = doc.createAttribute("ts");
    		attr.setValue(aspXmlDetais.getTs());
    		esign.setAttributeNode(attr);

    		attr = doc.createAttribute("txn");
    		attr.setValue(aspXmlDetais.getTxn());
    		esign.setAttributeNode(attr);

    		attr = doc.createAttribute("ekycId");
    		attr.setValue(aspXmlDetais.getEkycId());
    		esign.setAttributeNode(attr);	

    		attr = doc.createAttribute("ekycIdType");
    		attr.setValue(aspXmlDetais.getEkycIdType());
    		esign.setAttributeNode(attr);

    		attr = doc.createAttribute("aspId");
    		attr.setValue(aspXmlDetais.getAspId());
    		esign.setAttributeNode(attr);

    		attr = doc.createAttribute("AuthMode");
    		attr.setValue(aspXmlDetais.getAuthMode());
    		esign.setAttributeNode(attr);

    		attr = doc.createAttribute("responseSigType");
    		attr.setValue(aspXmlDetais.getResponseSigType());
    		esign.setAttributeNode(attr);

    		attr = doc.createAttribute("responseUrl");
    		attr.setValue(aspXmlDetais.getResponseUrl());
    		esign.setAttributeNode(attr);		

    		// Docs elements
    		Element docs = doc.createElement("Docs");
    		esign.appendChild(docs);

    		// InputHash elements
    		Element inputHash = doc.createElement("InputHash");

    		// set attribute to staff element
    		attr = doc.createAttribute("id");
    		attr.setValue(aspXmlDetais.getId());
    		inputHash.setAttributeNode(attr);

    		attr = doc.createAttribute("hashAlgorithm");
    		attr.setValue(aspXmlDetais.getHashAlgorithm());
    		inputHash.setAttributeNode(attr);

    		attr = doc.createAttribute("docInfo");
    		attr.setValue(aspXmlDetais.getDocInfo());
    		inputHash.setAttributeNode(attr);

    		inputHash.appendChild(doc.createTextNode(aspXmlDetais.getDocHashHex()));
    		docs.appendChild(inputHash);

    		// write the content into xml file
    		TransformerFactory transformerFactory = TransformerFactory.newInstance();
    		Transformer transformer = transformerFactory.newTransformer();
    		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    		DOMSource source = new DOMSource(doc);

    		try {

    			StringWriter writer = new StringWriter();
    			StreamResult result = new StreamResult(writer);
    			TransformerFactory tf = TransformerFactory.newInstance();
    			Transformer transformerTemp = tf.newTransformer();
    			transformerTemp.transform(source, result);
    			return writer.toString();
    			
    		} catch (Exception e) {
    			return "";
    		}
    	} catch (ParserConfigurationException pce) {
    		pce.printStackTrace();
    		return "";
    	} catch (TransformerException tfe) {
    		tfe.printStackTrace();
    		return "";
    	}
    }
}

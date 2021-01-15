package org.egov.lams.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.PrivateKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.egov.lams.models.pdfsign.FormXmlDataAsp;
import org.egov.lams.models.pdfsign.RequestXmlForm;
import org.egov.lams.util.PdfSignUtils;
import org.egov.lams.util.PdfSignXmlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;


public class PdfSignService {

	@Autowired
	private PdfSignUtils pdfSignUtils;
	
	@Autowired
	private PdfSignXmlUtils pdfSignXmlUtils;
	
	private RequestXmlForm doProcess(HttpServletRequest request, Model model,  HttpSession session) {
		System.out.println("**************************************"+session.getId());
		// PdfEmbedder pdfEmbedder = new PdfEmbedder();

			// Root Directory.
			String uploadRootPath = request.getServletContext().getRealPath("upload");
			System.out.println("uploadRootPath=" + uploadRootPath);

			File uploadRootDir = new File(uploadRootPath);
			// Create directory if it not exists.
			if (!uploadRootDir.exists()) {
				uploadRootDir.mkdirs();
			}
			//
			List<File> uploadedFiles = new ArrayList<File>();
			List<String> failedFiles = new ArrayList<String>();

			List<MultipartFile> fileDatas = new ArrayList<MultipartFile>();
			String fileHash = "";
			Random randNum = new Random();
			int randInt = randNum.nextInt();
			Calendar timenow  = Calendar.getInstance();
			String txnId = "" + timenow.getTimeInMillis() + "A" + randInt;
			txnId = txnId.replaceAll("-", "");
			System.out.println("txnid " + txnId);
			request.setAttribute("txnid", txnId);
			for (MultipartFile fileData : fileDatas) {

				// Client File Name
				String name = fileData.getOriginalFilename();
				System.out.println("Client File Name = " + name);

				if (name != null && name.length() > 0) {
					try {
						request.getSession().setAttribute("fname",uploadRootDir.getAbsolutePath() + File.separator + name);
						// Create the file at server
						File serverFile = new File(uploadRootDir.getAbsolutePath() + File.separator + txnId);

						BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
						stream.write(fileData.getBytes());
						stream.close();
						//
						uploadedFiles.add(serverFile);
						//fileHash = calculateFileHash(uploadRootDir.getAbsolutePath() + File.separator + name);
						fileHash = pdfSignUtils.pdfSigner(serverFile,request, session);
						System.out.println("Write file: " + serverFile);
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("Error Write file: " + name);
						failedFiles.add(name);
					}
				}
			}

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
			formXmalDataAsp.setResponseUrl("https://localhost:8443/SpringBootESign/finalResponse");
			formXmalDataAsp.setId("1");
			formXmalDataAsp.setHashAlgorithm("SHA256");
			formXmalDataAsp.setDocInfo("My Document");
			formXmalDataAsp.setDocHashHex(fileHash);

			//Get encrypted string/ signed data for xml signature tag
			String strToEncrypt = pdfSignXmlUtils.generateAspXml(formXmalDataAsp,request);
			String xmlData = "";
			try {
				PrivateKey rsaPrivateKey =  pdfSignUtils.getPrivateKey("D:\\\\pai\\\\egov\\\\cdacesign\\\\e-sign\\\\derkey.der");
				xmlData = pdfSignXmlUtils.signXmlStringNew(strToEncrypt, rsaPrivateKey);
				System.out.println(xmlData);
			}
			catch(Exception e) {
				System.out.println("Error in Encryption.");
				e.printStackTrace();
				return new RequestXmlForm();
			}

			RequestXmlForm myRequestXmlForm = new RequestXmlForm();
			myRequestXmlForm.setId("");
			myRequestXmlForm.setType("1");
			myRequestXmlForm.setDescription("Y");
			myRequestXmlForm.seteSignRequest(xmlData);
			myRequestXmlForm.setAspTxnID(txnId);
			myRequestXmlForm.setContentType("application/xml");
			return myRequestXmlForm;
	}   

}


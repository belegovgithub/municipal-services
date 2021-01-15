package org.egov.lams.models.pdfsign;
/*
Input field ID
Input field type
Description

eSignRequest
Text Area
This will contain a valid signed eSign request  XML

aspTxnID
Text Area
This should contain ASP transaction ID as in eSign request XML

Content-Type
Text Area
This attribute shall be set as Application/xml
*/
public class RequestXmlForm {
	public String id;
	public String type;
	public String description;
	public String eSignRequest;
	public String aspTxnID;
	public String contentType;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String geteSignRequest() {
		return eSignRequest;
	}
	public void seteSignRequest(String eSignRequest) {
		this.eSignRequest = eSignRequest;
	}
	public String getAspTxnID() {
		return aspTxnID;
	}
	public void setAspTxnID(String aspTxnID) {
		this.aspTxnID = aspTxnID;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	@Override
		public String toString() {
			// TODO Auto-generated method stub
			return super.toString();
		}
}

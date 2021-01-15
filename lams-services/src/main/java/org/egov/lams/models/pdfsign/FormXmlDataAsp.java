package org.egov.lams.models.pdfsign;



/*
<Esign ver ="" sc ="" ts="" txn="" ekycId="" ekycIdType="" aspId=""
AuthMode="" responseSigType="" responseUrl="">
<Docs>
<InputHash id=""
hashAlgorithm="" docInfo="">Document
Hash in Hex</InputHash>
</Docs>
<Signature>Digital signature of ASP</Signature>
</Esign>
*/
public class FormXmlDataAsp {
	public String ver;
	public String sc;
	public String ts;
	public String txn;
	public String ekycId;
	public String ekycIdType;
	public String aspId;
	public String authMode;
	public String responseSigType;
	public String responseUrl;
	public String id;
	public String hashAlgorithm;
	public String docInfo;
	public String docHashHex;
	public String digSigAsp;
	
	public String getVer() {
		return ver;
	}
	public void setVer(String ver) {
		this.ver = ver;
	}
	public String getSc() {
		return sc;
	}
	public void setSc(String sc) {
		this.sc = sc;
	}
	public String getTs() {
		return ts;
	}
	public void setTs(String ts) {
		this.ts = ts;
	}
	public String getTxn() {
		return txn;
	}
	public void setTxn(String txn) {
		this.txn = txn;
	}
	public String getEkycId() {
		return ekycId;
	}
	public void setEkycId(String ekycId) {
		this.ekycId = ekycId;
	}
	public String getEkycIdType() {
		return ekycIdType;
	}
	public void setEkycIdType(String ekycIdType) {
		this.ekycIdType = ekycIdType;
	}
	public String getAspId() {
		return aspId;
	}
	public void setAspId(String aspId) {
		this.aspId = aspId;
	}
	public String getAuthMode() {
		return authMode;
	}
	public void setAuthMode(String authMode) {
		this.authMode = authMode;
	}
	public String getResponseSigType() {
		return responseSigType;
	}
	public void setResponseSigType(String responseSigType) {
		this.responseSigType = responseSigType;
	}
	public String getResponseUrl() {
		return responseUrl;
	}
	public void setResponseUrl(String responseUrl) {
		this.responseUrl = responseUrl;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getHashAlgorithm() {
		return hashAlgorithm;
	}
	public void setHashAlgorithm(String hashAlgorithm) {
		this.hashAlgorithm = hashAlgorithm;
	}
	public String getDocInfo() {
		return docInfo;
	}
	public void setDocInfo(String docInfo) {
		this.docInfo = docInfo;
	}
	public String getDocHashHex() {
		return docHashHex;
	}
	public void setDocHashHex(String docHashHex) {
		this.docHashHex = docHashHex;
	}
	public String getDigSigAsp() {
		return digSigAsp;
	}
	public void setDigSigAsp(String digSigAsp) {
		this.digSigAsp = digSigAsp;
	}
}

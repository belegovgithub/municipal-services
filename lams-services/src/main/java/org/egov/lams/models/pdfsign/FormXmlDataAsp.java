package org.egov.lams.models.pdfsign;

import lombok.Getter;
import lombok.Setter;

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
@Getter
@Setter
public class FormXmlDataAsp {

	public String ver="2.1";
	public String sc="Y";
	public String ts;
	public String txn;
	public String ekycId="";
	public String ekycIdType="A";
	public String aspId="DGDE-001";
	public String authMode="1";
	public String responseSigType="pkcs7";
	public String responseUrl;
	public String id="1";
	public String hashAlgorithm="SHA256";
	public String docInfo;
	public String docHashHex;
	
}

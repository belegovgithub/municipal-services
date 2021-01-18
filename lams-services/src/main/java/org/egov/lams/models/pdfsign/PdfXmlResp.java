package org.egov.lams.models.pdfsign;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PdfXmlResp {

  @JsonProperty("dSignInfo")
  private RequestXmlForm dSignInfo;
  
  @JsonProperty("fileStoreInfo")
  private String fileStoreInfo;
  
  @JsonProperty("error")
  private String error;
  
}

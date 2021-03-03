package org.bel.birthdeath.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
public class BirthDeathConfiguration {


    //Idgen Config
    @Value("${egov.idgen.host}")
    private String idGenHost;

    @Value("${egov.idgen.path}")
    private String idGenPath;

    @Value("${egov.idgen.birthapplnum.name}")
    private String birthApplNumberIdgenName;

    @Value("${egov.idgen.birthapplnum.format}")
    private String birthApplNumberIdgenFormat;


    //Persister Config
    @Value("${persister.save.birth.topic}")
    private String saveBirthTopic;

    @Value("${persister.update.birth.topic}")
    private String updateBirthTopic;
    
    @Value("${persister.update.birthdownload.topic}")
    private String updateBirthDownloadTopic;


    //MDMS
    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsEndPoint;

    @Value("${egov.billingservice.host}")
    private String billingHost;

    @Value("${egov.bill.gen.endpoint}")
    private String fetchBillEndpoint;

    @Value("${egov.demand.create.endpoint}")
    private String demandCreateEndpoint;
    
    @Value("${egovpdf.host}")
    private String pdfHost;
    
    @Value("${egovpdf.birthcert.postendpoint}")
    private String saveBirthCertEndpoint;
    
    @Value("${egovpdf.deathcert.postendpoint}")
    private String saveDeathCertEndpoint;

}

package org.bel.obm.constants;

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
public class OBMConfiguration {

    @Value("${egov.idgen.host}")
    private String idGenHost;

    @Value("${egov.idgen.path}")
    private String idGenPath;

    @Value("${obm.chb.applno.name}")
    private String obmCHBookApplNumIdgenName;

    @Value("${obm.chb.applno.format}")
    private String obmCHBookApplNumIdgenFormat;
    
    @Value("${persister.save.obm.chb.topic}")
    private String saveOBMCHbookTopic;
    
    @Value("${persister.update.obm.chb.topic}")
    private String updateOBMCHbookTopic;
    
    @Value("${persister.update.obm.chb.workflow.topic}")
    private String updateOBMCHbookWorkflowTopic;
    
    @Value("${workflow.context.path}")
    private String wfHost;

    @Value("${workflow.transition.path}")
    private String wfTransitionPath;
    
    @Value("${workflow.businessservice.search.path}")
    private String wfBusinessServiceSearchPath;
    
    @Value("${egov.obm.default.limit}")
    private Integer defaultOBMLimit;

    @Value("${egov.obm.default.offset}")
    private Integer defaultOffset;

    @Value("${egov.obm.max.limit}")
    private Integer maxSearchLimit;    
    
    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsEndpoint;
}

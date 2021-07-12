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

    @Value("${egov.idgen.obmCHBookApplNum.name}")
    private String obmCHBookApplNumIdgenName;

    @Value("${egov.idgen.obmCHBookApplNum.format}")
    private String obmCHBookApplNumIdgenFormat;
    
    @Value("${persister.save.obm.chb.topic}")
    private String saveOBMCHbookTopic;
    
    @Value("${workflow.context.path}")
    private String wfHost;

    @Value("${workflow.transition.path}")
    private String wfTransitionPath;
    
    @Value("${egov.obm.default.limit}")
    private Integer defaultOBMLimit;

    @Value("${egov.obm.default.offset}")
    private Integer defaultOffset;

    @Value("${egov.obm.max.limit}")
    private Integer maxSearchLimit;    
}

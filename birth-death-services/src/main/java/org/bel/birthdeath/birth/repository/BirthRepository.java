package org.bel.birthdeath.birth.repository;


import java.util.ArrayList;
import java.util.List;

import org.bel.birthdeath.birth.certmodel.BirthCertRequest;
import org.bel.birthdeath.birth.model.EgBirthDtl;
import org.bel.birthdeath.birth.model.SearchCriteria;
import org.bel.birthdeath.birth.repository.builder.BirthDtlAllQueryBuilder;
import org.bel.birthdeath.birth.repository.builder.BirthDtlQueryBuilder;
import org.bel.birthdeath.birth.repository.rowmapper.BirthDtlsAllRowMapper;
import org.bel.birthdeath.birth.repository.rowmapper.BirthDtlsRowMapper;
import org.bel.birthdeath.common.calculation.demand.models.DemandRequest;
import org.bel.birthdeath.common.calculation.demand.models.DemandResponse;
import org.bel.birthdeath.common.contract.EgovPdfResp;
import org.bel.birthdeath.common.producer.Producer;
import org.bel.birthdeath.common.repository.ServiceRequestRepository;
import org.bel.birthdeath.config.BirthDeathConfiguration;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;


@Repository
public class BirthRepository {

	@Autowired
    private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private BirthDtlQueryBuilder queryBuilder;
	
	@Autowired
	private BirthDtlAllQueryBuilder allqueryBuilder;
	
	@Autowired
	private BirthDtlsRowMapper rowMapper;
	
	@Autowired
	private BirthDtlsAllRowMapper allRowMapper;
	
	@Autowired
	private Producer producer;
	
	@Autowired
	private BirthDeathConfiguration config;
	
	@Autowired
    private ServiceRequestRepository serviceRequestRepository;
	
	@Autowired
    private ObjectMapper mapper;
    
	public List<EgBirthDtl> getBirthDtls(SearchCriteria criteria) {
		List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getBirtDtls(criteria, preparedStmtList);
        List<EgBirthDtl> birthDtls =  jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
        return birthDtls;
	}

	public void save(BirthCertRequest birthCertRequest) {
		producer.push(config.getSaveBirthTopic(), birthCertRequest);
	}

	public EgovPdfResp saveBirthCertPdf(EgBirthDtl egBirthDtl) {
		StringBuilder url = new StringBuilder(config.getPdfHost());
        url.append(config.getSaveBirthCertEndpoint());
        Object result = serviceRequestRepository.fetchResult(url,egBirthDtl);
        EgovPdfResp response = null;
        try{
            response = mapper.convertValue(result,EgovPdfResp.class);
        }
        catch(IllegalArgumentException e){
            throw new CustomException("PARSING ERROR","Failed to parse response of create demand");
        }
        return response;
		
	}

	public List<EgBirthDtl> getBirthDtlsAll(SearchCriteria criteria) {
		List<Object> preparedStmtList = new ArrayList<>();
        String query = allqueryBuilder.getBirtDtlsAll(criteria, preparedStmtList);
        List<EgBirthDtl> birthDtls =  jdbcTemplate.query(query, preparedStmtList.toArray(), allRowMapper);
        return birthDtls;
	}

}

package org.bel.birthdeath.death.repository;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bel.birthdeath.common.contract.DeathPdfApplicationRequest;
import org.bel.birthdeath.common.contract.EgovPdfResp;
import org.bel.birthdeath.common.producer.Producer;
import org.bel.birthdeath.common.repository.ServiceRequestRepository;
import org.bel.birthdeath.config.BirthDeathConfiguration;
import org.bel.birthdeath.death.certmodel.DeathCertAppln;
import org.bel.birthdeath.death.certmodel.DeathCertRequest;
import org.bel.birthdeath.death.certmodel.DeathCertificate;
import org.bel.birthdeath.death.model.EgDeathDtl;
import org.bel.birthdeath.death.model.SearchCriteria;
import org.bel.birthdeath.death.repository.builder.DeathDtlAllQueryBuilder;
import org.bel.birthdeath.death.repository.rowmapper.DeathCertApplnRowMapper;
import org.bel.birthdeath.death.repository.rowmapper.DeathCertRowMapper;
import org.bel.birthdeath.death.repository.rowmapper.DeathDtlsAllRowMapper;
import org.bel.birthdeath.death.repository.rowmapper.DeathDtlsRowMapper;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;


@Repository
public class DeathRepository {

	@Autowired
    private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private DeathDtlAllQueryBuilder allqueryBuilder;
	
	@Autowired
	private DeathDtlsRowMapper rowMapper;
	
	@Autowired
	private DeathDtlsAllRowMapper allRowMapper;
	
	@Autowired
	private DeathCertRowMapper deathCertRowMapper;
	
	@Autowired
	private DeathCertApplnRowMapper certApplnRowMapper;
	
	@Autowired
	private Producer producer;
	
	@Autowired
	private BirthDeathConfiguration config;
	
	@Autowired
    private ServiceRequestRepository serviceRequestRepository;
	
	@Autowired
    private ObjectMapper mapper;
    
	public List<EgDeathDtl> getDeathDtls(SearchCriteria criteria) {
		List<Object> preparedStmtList = new ArrayList<>();
        String query = allqueryBuilder.getDeathDtls(criteria, preparedStmtList);
        List<EgDeathDtl> deathDtls =  jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
        return deathDtls;
	}

	public void save(DeathCertRequest deathCertRequest) {
		producer.push(config.getSaveDeathTopic(), deathCertRequest);
	}

	public EgovPdfResp saveDeathCertPdf(DeathPdfApplicationRequest pdfApplicationRequest) {
		/*StringBuilder url = new StringBuilder(config.getPdfHost());
        url.append(config.getSaveDeathCertEndpoint());
        Object result = serviceRequestRepository.fetchResult(url,egDeathDtl);
        EgovPdfResp response = null;
        try{
            response = mapper.convertValue(result,EgovPdfResp.class);
        }
        catch(IllegalArgumentException e){
            throw new CustomException("PARSING ERROR","Failed to parse response of create demand");
        }
        return response;*/
		System.out.println(new Gson().toJson(pdfApplicationRequest));
		RestTemplate restTemplate = new RestTemplate();
		MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
		mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_PDF, MediaType.APPLICATION_OCTET_STREAM));
		restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);
		String url = config.getPdfHost() + config.getSaveDeathCertEndpoint();
		HttpMethod requestMethod = HttpMethod.POST;
		HttpEntity<DeathPdfApplicationRequest> requestEntity = new HttpEntity<DeathPdfApplicationRequest>(pdfApplicationRequest);

		ResponseEntity<EgovPdfResp> response = restTemplate.exchange(url, requestMethod, requestEntity, EgovPdfResp.class);

		if(response.getStatusCode().equals(HttpStatus.OK)) {
			return response.getBody();
		}
		return null;
		
	}

	public List<EgDeathDtl> getDeathDtlsAll(SearchCriteria criteria) {
		List<Object> preparedStmtList = new ArrayList<>();
        String query = allqueryBuilder.getDeathDtlsAll(criteria, preparedStmtList);
        List<EgDeathDtl> deathDtls =  jdbcTemplate.query(query, preparedStmtList.toArray(), allRowMapper);
        return deathDtls;
	}

	public DeathCertificate getDeathCertReqByConsumerCode(String consumerCode) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = allqueryBuilder.getDeathCertReq(consumerCode,preparedStmtList);
		List<DeathCertificate> deathCerts =  jdbcTemplate.query(query, preparedStmtList.toArray(), deathCertRowMapper);
		return deathCerts.get(0);
	}

	public void updateCounter(String deathDtlId) {
		try {
			String updateQry="UPDATE public.eg_death_dtls SET counter=counter+1 WHERE id=?";
			jdbcTemplate.update(updateQry, deathDtlId);
		}catch(Exception e) {
			e.printStackTrace();
			throw new CustomException("Invalid_data","Error in updating");
		}
		
	}

	public List<DeathCertAppln> searchApplications( String uuid) {
		List<DeathCertAppln> deathCertAppls = new ArrayList<DeathCertAppln>();
		try {
			List<Object> preparedStmtList = new ArrayList<>();
			String applQuery=allqueryBuilder.searchApplications(uuid, preparedStmtList);
			deathCertAppls = jdbcTemplate.query(applQuery, preparedStmtList.toArray(), certApplnRowMapper);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new CustomException("Invalid_data","Error in updating");
		}
		return deathCertAppls;
		
	}

	public void updateDownloadStatus(DeathCertRequest certRequest) {
		producer.push(config.getUpdateDeathDownloadTopic(), certRequest);
	}
	
}

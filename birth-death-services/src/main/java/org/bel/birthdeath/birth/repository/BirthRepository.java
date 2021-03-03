package org.bel.birthdeath.birth.repository;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bel.birthdeath.birth.certmodel.BirthCertAppln;
import org.bel.birthdeath.birth.certmodel.BirthCertRequest;
import org.bel.birthdeath.birth.certmodel.BirthCertificate;
import org.bel.birthdeath.birth.model.EgBirthDtl;
import org.bel.birthdeath.birth.model.SearchCriteria;
import org.bel.birthdeath.birth.repository.builder.BirthDtlAllQueryBuilder;
import org.bel.birthdeath.birth.repository.rowmapper.BirthCertApplnRowMapper;
import org.bel.birthdeath.birth.repository.rowmapper.BirthCertRowMapper;
import org.bel.birthdeath.birth.repository.rowmapper.BirthDtlsAllRowMapper;
import org.bel.birthdeath.birth.repository.rowmapper.BirthDtlsRowMapper;
import org.bel.birthdeath.common.contract.BirthPdfApplicationRequest;
import org.bel.birthdeath.common.contract.EgovPdfResp;
import org.bel.birthdeath.common.producer.Producer;
import org.bel.birthdeath.common.repository.ServiceRequestRepository;
import org.bel.birthdeath.config.BirthDeathConfiguration;
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
public class BirthRepository {

	@Autowired
    private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private BirthDtlAllQueryBuilder allqueryBuilder;
	
	@Autowired
	private BirthDtlsRowMapper rowMapper;
	
	@Autowired
	private BirthDtlsAllRowMapper allRowMapper;
	
	@Autowired
	private BirthCertRowMapper birthCertRowMapper;
	
	@Autowired
	private BirthCertApplnRowMapper certApplnRowMapper;
	
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
        String query = allqueryBuilder.getBirtDtls(criteria, preparedStmtList);
        List<EgBirthDtl> birthDtls =  jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
        return birthDtls;
	}

	public void save(BirthCertRequest birthCertRequest) {
		producer.push(config.getSaveBirthTopic(), birthCertRequest);
	}

	public EgovPdfResp saveBirthCertPdf(BirthPdfApplicationRequest pdfApplicationRequest) {
		/*StringBuilder url = new StringBuilder(config.getPdfHost());
        url.append(config.getSaveBirthCertEndpoint());
        Object result = serviceRequestRepository.fetchResult(url,egBirthDtl);
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
		String url = config.getPdfHost() + config.getSaveBirthCertEndpoint();
		HttpMethod requestMethod = HttpMethod.POST;
		HttpEntity<BirthPdfApplicationRequest> requestEntity = new HttpEntity<BirthPdfApplicationRequest>(pdfApplicationRequest);

		ResponseEntity<EgovPdfResp> response = restTemplate.exchange(url, requestMethod, requestEntity, EgovPdfResp.class);

		if(response.getStatusCode().equals(HttpStatus.OK)) {
			return response.getBody();
		}
		return null;
		
	}

	public List<EgBirthDtl> getBirthDtlsAll(SearchCriteria criteria) {
		List<Object> preparedStmtList = new ArrayList<>();
        String query = allqueryBuilder.getBirtDtlsAll(criteria, preparedStmtList);
        List<EgBirthDtl> birthDtls =  jdbcTemplate.query(query, preparedStmtList.toArray(), allRowMapper);
        return birthDtls;
	}

	public BirthCertificate getBirthCertReqByConsumerCode(String consumerCode) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = allqueryBuilder.getBirthCertReq(consumerCode,preparedStmtList);
		List<BirthCertificate> birthCerts =  jdbcTemplate.query(query, preparedStmtList.toArray(), birthCertRowMapper);
		return birthCerts.get(0);
	}

	public void updateCounter(String birthDtlId) {
		try {
			String updateQry="UPDATE public.eg_birth_dtls SET counter=counter+1 WHERE id=?";
			jdbcTemplate.update(updateQry, birthDtlId);
		}catch(Exception e) {
			e.printStackTrace();
			throw new CustomException("Invalid_data","Error in updating");
		}
		
	}

	public List<BirthCertAppln> searchApplications(String tenantId, String uuid) {
		List<BirthCertAppln> birthCertAppls = new ArrayList<BirthCertAppln>();
		try {
			List<Object> preparedStmtList = new ArrayList<>();
			String applQuery=allqueryBuilder.searchApplications(tenantId, uuid, preparedStmtList);
			birthCertAppls = jdbcTemplate.query(applQuery, preparedStmtList.toArray(), certApplnRowMapper);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new CustomException("Invalid_data","Error in updating");
		}
		return birthCertAppls;
		
	}

	public void updateDownloadStatus(BirthCertificate birthCert) {
		producer.push(config.getUpdateBirthDownloadTopic(), birthCert);
	}
	
}

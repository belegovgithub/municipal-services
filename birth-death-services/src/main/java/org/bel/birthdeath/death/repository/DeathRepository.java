package org.bel.birthdeath.death.repository;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bel.birthdeath.common.contract.DeathPdfApplicationRequest;
import org.bel.birthdeath.common.contract.EgovPdfResp;
import org.bel.birthdeath.common.producer.BndProducer;
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
import org.egov.common.contract.request.RequestInfo;
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
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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
	private BndProducer bndProducer;
	
	@Autowired
	private BirthDeathConfiguration config;
	
	@Autowired
    private RestTemplate restTemplate;
	
	public List<EgDeathDtl> getDeathDtls(SearchCriteria criteria) {
		List<Object> preparedStmtList = new ArrayList<>();
        String query = allqueryBuilder.getDeathDtls(criteria, preparedStmtList);
        List<EgDeathDtl> deathDtls =  jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
        return deathDtls;
	}

	public void save(DeathCertRequest deathCertRequest) {
		bndProducer.push(config.getSaveDeathTopic(), deathCertRequest);
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
		try {
			SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");	
			pdfApplicationRequest.getDeathCertificate().forEach(cert-> {
				String UIHost = config.getUiAppHost();
				String deathCertPath = config.getDeathCertLink();
				deathCertPath = deathCertPath.replace("$id",cert.getId());
				deathCertPath = deathCertPath.replace("$tenantId",cert.getTenantid());
				deathCertPath = deathCertPath.replace("$regNo",cert.getRegistrationno());
				deathCertPath = deathCertPath.replace("$dateofdeath",format.format(cert.getDateofdeath()));
				deathCertPath = deathCertPath.replace("$gender",cert.getGender().toString());
				deathCertPath = deathCertPath.replace("$deathcertificateno",cert.getDeathcertificateno());
				String finalPath = UIHost + deathCertPath;
				cert.setEmbeddedUrl(getShortenedUrl(finalPath));
	        });
		
		log.info(new Gson().toJson(pdfApplicationRequest));
		
		//RestTemplate restTemplate = new RestTemplate();
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
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
		
	}

	public List<EgDeathDtl> getDeathDtlsAll(SearchCriteria criteria) {
		List<Object> preparedStmtList = new ArrayList<>();
        String query = allqueryBuilder.getDeathDtlsAll(criteria, preparedStmtList);
        List<EgDeathDtl> deathDtls =  jdbcTemplate.query(query, preparedStmtList.toArray(), allRowMapper);
        return deathDtls;
	}

	public DeathCertificate getDeathCertReqByConsumerCode(String consumerCode, RequestInfo requestInfo) {
		try {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = allqueryBuilder.getDeathCertReq(consumerCode,requestInfo,preparedStmtList);
		List<DeathCertificate> deathCerts =  jdbcTemplate.query(query, preparedStmtList.toArray(), deathCertRowMapper);
		if(null!=deathCerts && !deathCerts.isEmpty())
			return deathCerts.get(0);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new CustomException("invalid_data","Invalid Data");
		}
		return null;
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

	public void update(DeathCertRequest certRequest) {
		bndProducer.push(config.getUpdateDeathTopic(), certRequest);
	}
	
	public String getShortenedUrl(String url){
		HashMap<String,String> body = new HashMap<>();
		body.put("url",url);
		StringBuilder builder = new StringBuilder(config.getUrlShortnerHost());
		builder.append(config.getUrlShortnerEndpoint());
		String res = restTemplate.postForObject(builder.toString(), body, String.class);
		if(StringUtils.isEmpty(res)){
			log.error("URL_SHORTENING_ERROR","Unable to shorten url: "+url); ;
			return url;
		}
		else return res;
	}
	
	public List<EgDeathDtl> viewCertificateData(SearchCriteria criteria) {
		List<EgDeathDtl> certData = new ArrayList<EgDeathDtl>(); 
		DeathCertificate certificate = getDeathCertReqByConsumerCode(criteria.getDeathcertificateno(),null);
		criteria.setId(certificate.getDeathDtlId());
		certData= getDeathDtlsAll(criteria);
		certData.get(0).setDateofissue(certificate.getDateofissue());
		return certData;
	}
}

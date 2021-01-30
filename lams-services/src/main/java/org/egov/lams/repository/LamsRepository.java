package org.egov.lams.repository;


import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.lams.config.LamsConfiguration;
import org.egov.lams.model.SearchCriteria;
import org.egov.lams.models.pdfsign.LamsEsignDtls;
import org.egov.lams.producer.Producer;
import org.egov.lams.repository.builder.LamsQueryBuilder;
import org.egov.lams.repository.builder.LamsQueryBuilderMaster;
import org.egov.lams.rowmapper.LamsRowMapper;
import org.egov.lams.rowmapper.LamsRowMapperMaster;
import org.egov.lams.web.models.AuditDetails;
import org.egov.lams.web.models.EsignLamsRequest;
import org.egov.lams.web.models.LamsRequest;
import org.egov.lams.web.models.LeaseAgreementRenewal;
import org.egov.lams.web.models.LeaseAgreementRenewalDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Repository
public class LamsRepository {

    private Producer producer;
    
    private LamsConfiguration config;

    private JdbcTemplate jdbcTemplate;
    
    private NamedParameterJdbcTemplate parameterJdbcTemplate;
    
    private LamsQueryBuilder queryBuilder;
    
    private LamsQueryBuilderMaster queryBuildermaster;

    private LamsRowMapper rowMapper;
    
    private LamsRowMapperMaster rowMapperMaster;
    
    private RestTemplate restTemplate;

    @Autowired
    public LamsRepository(Producer producer, LamsConfiguration config,LamsQueryBuilder queryBuilder,LamsQueryBuilderMaster queryBuildermaster,
    		JdbcTemplate jdbcTemplate,LamsRowMapper rowMapper,LamsRowMapperMaster rowMapperMaster ,RestTemplate restTemplate,
    		NamedParameterJdbcTemplate parameterJdbcTemplate) {
        this.producer = producer;
        this.config = config;
        this.jdbcTemplate = jdbcTemplate;
        this.queryBuilder = queryBuilder ; 
        this.queryBuildermaster = queryBuildermaster;
        this.parameterJdbcTemplate = parameterJdbcTemplate;
        this.rowMapper = rowMapper;
        this.rowMapperMaster=rowMapperMaster;
        this.restTemplate = restTemplate;
    }

    public void save(LamsRequest lamsRequest) {
    	
        producer.push(config.getSaveLamsLRTopic(), lamsRequest);
    }
    
    public void update(LamsRequest lamsRequest, Map<String, Boolean> idToIsStateUpdatableMap) {
    	RequestInfo requestInfo = lamsRequest.getRequestInfo();
        List<LeaseAgreementRenewal> leases = lamsRequest.getLeases();

        List<LeaseAgreementRenewal> leasesForStatusUpdate = new LinkedList<>();
        List<LeaseAgreementRenewal> leasesForUpdate = new LinkedList<>();


        for (LeaseAgreementRenewal lease : leases) {
        	/*if(LRConstants.ACTION_APPROVE.equals(lease.getAction())) {
        		log.info("Updating Mst");
        		producer.push(config.getUpdateLamsSurveyTopic(), lamsRequest);
        	}*/
            if (idToIsStateUpdatableMap.get(lease.getId())) {
                leasesForUpdate.add(lease);
            }
            else {
                leasesForStatusUpdate.add(lease);
            }
        }
        if (!CollectionUtils.isEmpty(leasesForUpdate))
            producer.push(config.getUpdateLamsLRTopic(), new LamsRequest(requestInfo, leasesForUpdate));

        if (!CollectionUtils.isEmpty(leasesForStatusUpdate))
            producer.push(config.getUpdateLamsLRWorkflowTopic(), new LamsRequest(requestInfo, leasesForStatusUpdate));

    }
    
    
    public List<LeaseAgreementRenewal> getLeaseRenewals(SearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getLeaseRenewalSearchQuery(criteria, preparedStmtList);
        List<LeaseAgreementRenewal> leases =  jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
        return leases;
    }

	public List<LeaseAgreementRenewalDetail> getLeaseDetails(SearchCriteria criteria) {
		List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuildermaster.getLeaseDetails(criteria, preparedStmtList);
        List<LeaseAgreementRenewalDetail> leases =  jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapperMaster);
        return leases;
	}

	public void deleteApplDocs(List<String> docIdsStored) {
        String query = queryBuilder.deleteApplDocs();
        Map namedParameters = Collections.singletonMap("ids", docIdsStored);
        parameterJdbcTemplate.update(query, namedParameters);
	}
    
	public void saveEsignDtls(EsignLamsRequest esignRequest) {
        producer.push(config.getSaveLamsEsignTopic(), esignRequest);
    }

	public void updateEsignDtls(EsignLamsRequest esignRequest) {
		//producer.push(config.getUpdateLamsEsignTopic(), esignRequest);
		try {
		LamsEsignDtls le =esignRequest.getLamsEsignDtls();
		String updateSQL = "UPDATE eg_lams_esign_detail SET errorcode = ?, filestoreid =? , status = ?,  lastmodifiedtime = ? WHERE txnid=? ;";
		int Updresult = jdbcTemplate.update(updateSQL, le.getErrorCode(),le.getFileStoreId(),le.getStatus(),le.getAuditDetails().getLastModifiedTime(),le.getTxnId());
		String insertSQL = "INSERT INTO eg_lams_esign_detail_audit SELECT * FROM eg_lams_esign_detail WHERE txnid = ?";
		int insResult = jdbcTemplate.update(insertSQL, le.getTxnId());
		log.info("insresult : "+insResult +" and updResult : "+Updresult);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public String getApplicationfile(String txnid) {
		String sql = "SELECT filestoreid FROM eg_lams_esign_detail WHERE txnid=?";

	    String filestoreid = (String) jdbcTemplate.queryForObject(
	            sql, new Object[] { txnid }, String.class);
	    JsonObject obj= new JsonObject();
	    JsonArray array = new JsonArray();
	    JsonObject o = new JsonObject();
	    o.addProperty("fileStoreId", filestoreid);
	    o.addProperty("tenantId", "pb");
	    array.add(o);
	    obj.add("files", array);
	    return new Gson().toJson(obj);
	}
}

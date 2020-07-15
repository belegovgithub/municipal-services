package org.egov.pg.repository;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.User;
import org.egov.pg.repository.builder.PgDetailQueryBuilder;
import org.egov.pg.repository.rowmapper.BillingSlabRowMapper;
import org.egov.pg.repository.rowmapper.PgDetailRowMapper;
import org.egov.pg.web.contract.PgDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import static org.egov.pg.repository.builder.PgDetailQueryBuilder.SELECT_NEXT_SEQUENCE_PGDETAIL;
@Repository
@Slf4j
public class PgDetailRepository {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	private BillingSlabRowMapper billingSlabRowMapper;
	
	@Autowired
	private PgDetailQueryBuilder pgDetailQueryBuilder;
	
	/**
	 * Queries the db with search query using a connection abstracted within the jdbctemplate.
	 * @param query
	 * @param preparedStmtList
	 * @return List<BillingSlab>
	 */
	public List<PgDetail> getDataFromDB(String query, List<Object> preparedStmtList){
		List<PgDetail> slabs = new ArrayList<>();
		try {
			slabs = jdbcTemplate.query(query, preparedStmtList.toArray(), billingSlabRowMapper);
			if(CollectionUtils.isEmpty(slabs))
				return new ArrayList<>();
		}catch(Exception e) {
			log.error("Exception while fetching from DB: " + e);
			return slabs;
		}

		return slabs;
	}
	
	private Long getNextSequence() {
		return jdbcTemplate.queryForObject(SELECT_NEXT_SEQUENCE_PGDETAIL, Long.class);
	}

	
	
	public List<PgDetail> getPgDetails(List<PgDetail>pgDetailList) {
		final Map<String, Object> Map = new HashMap<String, Object>();
		Map.put("tenantId", pgDetailList.get(0).getTenantId());
		List<PgDetail> pgDetailListResponse = namedParameterJdbcTemplate.query(PgDetailQueryBuilder.GET_PGDETAIL_BY_TENANTID, Map,new PgDetailRowMapper());
		return pgDetailListResponse;
		
	
		
	}
	
	
	public List<PgDetail> createPgDetails(User user, List<PgDetail> pgDetails ) {
		final Long newId = getNextSequence();
		PgDetail pgDetail = pgDetails.get(0);
		pgDetail.setId(newId);
		pgDetail.setCreatedDate(new Date());
		pgDetail.setLastModifiedDate(new Date());
		pgDetail.setCreatedBy(user.getUuid());
		PgDetail  pg= save(pgDetail);
		if(pg!=null) {
			return pgDetails;
		}
		return null;
	}
	
	public PgDetail save(PgDetail pgDetail) {
		if(pgDetail!=null) {
			Map<String,Object>pgDetilInputs = new HashMap<String, Object>();
			pgDetilInputs.put("id", pgDetail.getId());
			pgDetilInputs.put("tenantid", pgDetail.getTenantId());
			pgDetilInputs.put("merchantid", pgDetail.getMerchantId());
			pgDetilInputs.put("merchantSecretKey", pgDetail.getMerchantSecretKey());
			pgDetilInputs.put("merchantUserName", pgDetail.getMerchantUserName());
			pgDetilInputs.put("merchantPassword", pgDetail.getMerchantPassword());
			pgDetilInputs.put("merchantServiceId", pgDetail.getMerchantServiceId());
			pgDetilInputs.put("createddate", pgDetail.getCreatedDate());
			pgDetilInputs.put("lastmodifieddate", pgDetail.getLastModifiedDate());
			pgDetilInputs.put("createdby", pgDetail.getCreatedBy());
			pgDetilInputs.put("lastmodifiedby", pgDetail.getLastModifiedBy());
			int result = namedParameterJdbcTemplate.update(pgDetailQueryBuilder.getInsertUserQuery(), pgDetilInputs);
			if(result!=0) {
				return pgDetail;
			}
		}
		return null;
		
	}

}
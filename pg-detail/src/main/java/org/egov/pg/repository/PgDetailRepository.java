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
	
	
	public List<PgDetail> createPgDetails(User user,List<PgDetail>pgDetailList) {
		List<PgDetail>pgDetailListResult = new ArrayList<PgDetail>();
		if(pgDetailList.size()>0) {
			for(PgDetail eachPgDetail:pgDetailList) {
				final Long newId = getNextSequence();
				eachPgDetail.setId(newId);
				eachPgDetail.setCreatedDate(new Date());
				eachPgDetail.setLastModifiedDate(new Date());
				eachPgDetail.setCreatedBy(user.getId());
				eachPgDetail.setLastModifiedBy(user.getId());
				String savedPgDetailResult = save(eachPgDetail);
				if(savedPgDetailResult.equals("success")) {
					pgDetailListResult.add(eachPgDetail);
				}
				else {
					System.out.println("error while saving pgDetail");
					pgDetailListResult.clear();
					return pgDetailListResult;
				}
				
			}
		}
		System.out.println("All went well, so returning the success list");
		return pgDetailListResult;
	}
	
	public String save(PgDetail pgDetail) {
		Map<String,Object>pgDetilInputs = new HashMap<String, Object>();
		pgDetilInputs.put("id", pgDetail.getId());
		pgDetilInputs.put("tenantid", pgDetail.getTenantId());
		pgDetilInputs.put("merchantid", pgDetail.getMerchantId());
		pgDetilInputs.put("secretkey", pgDetail.getSecretKey());
		pgDetilInputs.put("username", pgDetail.getUserName());
		pgDetilInputs.put("password", pgDetail.getPassword());
		pgDetilInputs.put("createddate", pgDetail.getCreatedDate());
		pgDetilInputs.put("lastmodifieddate", pgDetail.getLastModifiedDate());
		pgDetilInputs.put("createdby", pgDetail.getCreatedBy());
		pgDetilInputs.put("lastmodifiedby", pgDetail.getLastModifiedBy());
		int result = namedParameterJdbcTemplate.update(pgDetailQueryBuilder.getInsertUserQuery(), pgDetilInputs);
		if(result !=0) {
			System.out.println("Saved successfully");
			return "success";
			
		}
		return "error";
		
	}

}
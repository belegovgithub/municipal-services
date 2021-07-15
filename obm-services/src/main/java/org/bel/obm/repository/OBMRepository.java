package org.bel.obm.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bel.obm.constants.OBMConfiguration;
import org.bel.obm.models.CHBookDtls;
import org.bel.obm.models.CHBookRequest;
import org.bel.obm.models.SearchCriteria;
import org.bel.obm.producer.Producer;
import org.bel.obm.repository.querybuilder.OBMQueryBuilder;
import org.bel.obm.rowmapper.CHBookDtlsRowMapper;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class OBMRepository {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private OBMQueryBuilder queryBuilder;

	@Autowired
	private CHBookDtlsRowMapper chrowMapper;

	@Autowired
	private Producer producer;

	@Autowired
	private OBMConfiguration config;

	@Autowired
	private NamedParameterJdbcTemplate parameterJdbcTemplate;

	public void save(CHBookRequest request) {
		producer.push(config.getSaveOBMCHbookTopic(), request);
	}

	public List<CHBookDtls> getCHBookDts(SearchCriteria criteria) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = queryBuilder.getCHBookDtsSearchQuery(criteria, preparedStmtList);
		List<CHBookDtls> bookDtls = jdbcTemplate.query(query, preparedStmtList.toArray(), chrowMapper);
		return bookDtls;
	}

	public void deleteApplDocs(List<String> docIdstoDelete) {
		String query = queryBuilder.deleteApplDocsCHB();
		Map namedParameters = Collections.singletonMap("ids", docIdstoDelete);
		parameterJdbcTemplate.update(query, namedParameters);
	}

	public void update(CHBookRequest chBookRequest, Map<String, Boolean> idToIsStateUpdatableMap) {
		RequestInfo requestInfo = chBookRequest.getRequestInfo();

		CHBookDtls chBookDtls = chBookRequest.getBooking();
		if (idToIsStateUpdatableMap.get(chBookDtls.getId())) {
			producer.push(config.getUpdateOBMCHbookTopic(), new CHBookRequest(requestInfo, chBookDtls));
		} else {
			producer.push(config.getUpdateOBMCHbookWorkflowTopic(), new CHBookRequest(requestInfo, chBookDtls));
		}

	}
}

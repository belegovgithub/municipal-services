package org.bel.obm.repository;


import java.util.ArrayList;
import java.util.List;

import org.bel.obm.constants.OBMConfiguration;
import org.bel.obm.models.CHBookDtls;
import org.bel.obm.models.CHBookRequest;
import org.bel.obm.models.SearchCriteria;
import org.bel.obm.producer.Producer;
import org.bel.obm.repository.querybuilder.OBMQueryBuilder;
import org.bel.obm.rowmapper.CHBookDtlsRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
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
	
	public void save(CHBookRequest request) {
		producer.push(config.getSaveOBMCHbookTopic(), request);
	}

	public List<CHBookDtls> getCHBookDts(SearchCriteria criteria) {
		List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getCHBookDtsSearchQuery(criteria, preparedStmtList);
        List<CHBookDtls> bookDtls =  jdbcTemplate.query(query, preparedStmtList.toArray(), chrowMapper);
        return bookDtls;
	}
}

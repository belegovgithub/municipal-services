package org.bel.birthdeath.birth.repository;


import java.util.ArrayList;
import java.util.List;

import org.bel.birthdeath.birth.model.EgBirthDtl;
import org.bel.birthdeath.birth.model.SearchCriteria;
import org.bel.birthdeath.birth.repository.builder.BirthDtlQueryBuilder;
import org.bel.birthdeath.birth.repository.rowmapper.BirthDtlsRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
public class BirthRepository {

	@Autowired
    private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private BirthDtlQueryBuilder queryBuilder;
	
	@Autowired
	private BirthDtlsRowMapper rowMapper;
    
	public List<EgBirthDtl> getBirthDtls(SearchCriteria criteria) {
		List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getBirtDtls(criteria, preparedStmtList);
        List<EgBirthDtl> birthDtls =  jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
        return birthDtls;
	}

}

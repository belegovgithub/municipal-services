package org.bel.birthdeath.birth.service;

import java.util.List;

import org.bel.birthdeath.birth.model.EgBirthDtl;
import org.bel.birthdeath.birth.model.SearchCriteria;
import org.bel.birthdeath.birth.repository.BirthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BirthService {
	
	@Autowired
	BirthRepository repository;

	public List<EgBirthDtl> search(SearchCriteria criteria) {
		List<EgBirthDtl> birthDtls = null;
		birthDtls = repository.getBirthDtls(criteria);
		return birthDtls;
	}
}

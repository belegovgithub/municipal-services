package org.bel.obm.controllers;

import java.util.Arrays;
import java.util.List;

import org.bel.obm.models.CHBookDtls;
import org.bel.obm.models.CHBookRequest;
import org.bel.obm.models.CHBookResponse;
import org.bel.obm.models.RequestInfoWrapper;
import org.bel.obm.models.SearchCriteria;
import org.bel.obm.services.CHBookService;
import org.bel.obm.util.ResponseInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chb")
public class CHBookController {

	@Autowired
	private ResponseInfoFactory responseInfoFactory;

	@Autowired
	private CHBookService chBookService;

	@PostMapping("/_create")
	public ResponseEntity<CHBookResponse> create(@RequestBody CHBookRequest cHBookRequest) {
		CHBookDtls chBookDtls = chBookService.create(cHBookRequest);
		CHBookResponse response = CHBookResponse.builder().booking(Arrays.asList(chBookDtls))
				.responseInfo(
						responseInfoFactory.createResponseInfoFromRequestInfo(cHBookRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping({ "/_search" })
	public ResponseEntity<CHBookResponse> search(@RequestBody RequestInfoWrapper requestInfoWrapper,
			@ModelAttribute SearchCriteria criteria) {
		List<CHBookDtls> chBookDtlsList = chBookService.search(criteria, requestInfoWrapper.getRequestInfo());
		CHBookResponse response = CHBookResponse.builder().booking(chBookDtlsList).responseInfo(
				responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping({ "/_update" })
	public ResponseEntity<CHBookResponse> update(@RequestBody CHBookRequest chbRequest) {
		CHBookDtls chBookDtls = chBookService.update(chbRequest);
		CHBookResponse response = CHBookResponse.builder().booking(Arrays.asList(chBookDtls))
				.responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(chbRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}

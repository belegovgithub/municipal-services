package org.bel.obm.services;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bel.obm.calculation.Calculation;
import org.bel.obm.calculation.CalculationReq;
import org.bel.obm.calculation.CalculationRes;
import org.bel.obm.calculation.CalulationCriteria;
import org.bel.obm.models.CHBookDtls;
import org.bel.obm.models.CHBookRequest;
import org.bel.obm.repository.ServiceRequestRepository;
import org.bel.obm.util.CommonUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CalculationService {

	@Autowired
	private CommonUtils utils;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private ObjectMapper mapper;

	public CHBookDtls addCalculation(CHBookRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		CHBookDtls chBookDtls = request.getBooking();

		if (null == chBookDtls)
			throw new CustomException("INVALID REQUEST", "The request for calculation cannot be empty or null");

		CalculationRes response = getCalculation(requestInfo, chBookDtls);
		List<Calculation> calculations = response.getCalculations();
		Map<String, Calculation> applicationNumberToCalculation = new HashMap<>();
		calculations.forEach(calculation -> {
			applicationNumberToCalculation.put(calculation.getChBookDtls().getApplicationNumber(), calculation);
			calculation.setChBookDtls(null);
		});

		chBookDtls.setCalculation(applicationNumberToCalculation.get(chBookDtls.getApplicationNumber()));

		return chBookDtls;
	}

	private CalculationRes getCalculation(RequestInfo requestInfo, CHBookDtls chBookDtls) {
		StringBuilder uri = utils.getCalculationURI();
		List<CalulationCriteria> criterias = new LinkedList<>();

		criterias.add(new CalulationCriteria(chBookDtls, chBookDtls.getApplicationNumber(), chBookDtls.getTenantId()));

		CalculationReq request = CalculationReq.builder().calulationCriteria(criterias).requestInfo(requestInfo)
				.build();

		Object result = serviceRequestRepository.fetchResult(uri, request);
		CalculationRes response = null;
		try {
			response = mapper.convertValue(result, CalculationRes.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException("PARSING ERROR", "Failed to parse response of calculate");
		}
		return response;
	}

}

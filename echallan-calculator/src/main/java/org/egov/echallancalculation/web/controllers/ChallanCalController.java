package org.egov.echallancalculation.web.controllers;


import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.echallancalculation.service.CalculationService;
import org.egov.echallancalculation.web.models.calculation.Calculation;
import org.egov.echallancalculation.web.models.calculation.CalculationReq;
import org.egov.echallancalculation.web.models.calculation.CalculationRes;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class ChallanCalController {
	@Autowired
	private CalculationService calculationService;	
	
	@PostMapping("/_calculate")
	public ResponseEntity<CalculationRes> calculate(@Valid @RequestBody CalculationReq calculationReq,@PathVariable(required = false) String servicename) {

		List<Calculation> calculations = null;
		calculations = calculationService.getCalculation(calculationReq);

		CalculationRes calculationRes = CalculationRes.builder().calculations(calculations).build();
		  System.out.println("calculationRes---"+calculationRes.getCalculations().toString());
		return new ResponseEntity<CalculationRes>(calculationRes, HttpStatus.OK);
	}


}

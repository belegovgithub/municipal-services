package org.egov.pg.web.controller;

import javax.validation.Valid;

import org.egov.pg.service.PgDetailService;
import org.egov.pg.validator.PgDetailValidator;
import org.egov.pg.web.contract.PgDetailRequest;
import org.egov.pg.web.contract.PgDetailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
 

@Controller
//@RequestMapping("/pgdetail")
public class PgDetailController {

	@Autowired
	private PgDetailValidator pgDetailValidator;

	@Autowired
	private PgDetailService service;

 

	@Autowired
	private org.egov.pg.utils.ResponseInfoFactory factory;
	/**
	 * Creates Payment Gateway details for cantonment board
	 * @param pgDetailRequest
	 * @return
	 */
	@RequestMapping(value = "/_create", method = RequestMethod.POST)
	public ResponseEntity<PgDetailResponse> createPgDetails(@Valid @RequestBody PgDetailRequest pgDetailRequest) {
		PgDetailResponse response = service.createPgDetails(pgDetailRequest);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}
	
	@RequestMapping(value = "/_getPgDetail", method = RequestMethod.POST)
	public ResponseEntity<PgDetailResponse> getPgDetails(@RequestBody PgDetailRequest pgDetailRequest){
		PgDetailResponse response = service.getPgDetails(pgDetailRequest);
		return new ResponseEntity<>(response, HttpStatus.OK);
		
	}
	
	
	
//
//	/**
//	 * Updates Billing Slabs of TradeLicense
//	 * @param billingSlabReq
//	 * @return
//	 */
//	@RequestMapping(value = "/_update", method = RequestMethod.POST)
//	public ResponseEntity<BillingSlabRes> billingslabUpdatePost(@Valid @RequestBody BillingSlabReq billingSlabReq) {
//		billingslabValidator.validateUpdate(billingSlabReq);
//		BillingSlabRes response = service.updateSlabs(billingSlabReq);
//		return new ResponseEntity<BillingSlabRes>(response, HttpStatus.OK);
//	}
//
//	/**
//	 * Searches Billing Slabs belonging TradeLicense based on criteria
//	 * @param billingSlabSearchCriteria
//	 * @param requestInfo
//	 * @return
//	 */
//    @RequestMapping(value = {"/{servicename}/_search", "/_search"}, method = RequestMethod.POST)
//    public ResponseEntity<BillingSlabRes> billingslabSearchPost(@ModelAttribute @Valid BillingSlabSearchCriteria billingSlabSearchCriteria,
//                                                                @Valid @RequestBody RequestInfo requestInfo,@PathVariable(required = false) String servicename) {
//		if(servicename==null)
//			servicename = businessService_TL;
//
//		BillingSlabRes response = null;
//		switch(servicename)
//		{
//			case businessService_TL:
//				response = service.searchSlabs(billingSlabSearchCriteria, requestInfo);
//				break;
//
//			case businessService_BPA:
//				BillingSlab billingSlab = bpaBillingSlabService.search(billingSlabSearchCriteria, requestInfo);
//				response = BillingSlabRes.builder().responseInfo(factory.createResponseInfoFromRequestInfo(requestInfo, true))
//						.billingSlab(Collections.singletonList(billingSlab)).build();
//				break;
//
//			default:
//				throw new CustomException("UNKNOWN_BUSINESSSERVICE", " Business Service not supported");
//		}
//        return new ResponseEntity<BillingSlabRes>(response, HttpStatus.OK);
//    }

}

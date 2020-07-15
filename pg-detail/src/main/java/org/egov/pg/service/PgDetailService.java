
package org.egov.pg.service;
import java.util.ArrayList;
import java.util.List;


import org.egov.common.contract.request.User;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.pg.domain.service.utils.EncryptionDecryptionUtil;
import org.egov.pg.repository.PgDetailRepository;
import org.egov.pg.utils.PgDetailUtils;
import org.egov.pg.utils.ResponseInfoFactory;
import org.egov.pg.web.contract.PgDetail;
import org.egov.pg.web.contract.PgDetailRequest;
import org.egov.pg.web.contract.PgDetailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;

import lombok.extern.slf4j.Slf4j; 
@Service
@Slf4j
public class PgDetailService {
	
	@Autowired
	private PgDetailUtils util;
	
	@Autowired
	private RestTemplate restTemplate;
	
	 
	@Autowired
	private ResponseInfoFactory responseInfoFactory;
	
	@Autowired
	private PgDetailRepository repository;
	
	@Autowired
	private EncryptionDecryptionUtil encryptionDecryptionUtil;
//	@Autowired
//	private BillingslabQueryBuilder queryBuilder;
//	
//	@Autowired
//	private BillingSlabConfigs billingSlabConfigs;
	
	/**
	 * Service layer for creating billing slabs
	 * @param billingSlabReq
	 * @return
	 */
	public PgDetailResponse createPgDetails(PgDetailRequest pgDetailRequest) {
		PgDetail pg =encryptionDecryptionUtil.encryptObject(pgDetailRequest.getPgDetail().get(0) , "PgDetail",PgDetail.class);
		System.out.println("Encyription "+pg);
		List<PgDetail> pgList = new ArrayList<PgDetail>();
		pgList.add(pg);
		User userInfo = pgDetailRequest.getRequestInfo().getUserInfo();
		List<PgDetail> pgDetailListResponse = repository.createPgDetails(userInfo,pgList);
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(pgDetailRequest.getRequestInfo(), true);
		return new PgDetailResponse(responseInfo, pgDetailListResponse);
		
	}
	
   public PgDetailResponse getPgDetails(PgDetailRequest pgDetailRequest) {
	   List<PgDetail> pgDetailListResponse = repository.getPgDetails(pgDetailRequest.getPgDetail());
	   ArrayList<PgDetail> decyrptList = new ArrayList<PgDetail>();
	   if(pgDetailListResponse!=null && pgDetailListResponse.size()>0)
	   for (PgDetail pgDetail : pgDetailListResponse) {
		   decyrptList.add(encryptionDecryptionUtil.decryptObject(pgDetail, "PgDetail", PgDetail.class, pgDetailRequest.getRequestInfo()));
	   }
	   ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(pgDetailRequest.getRequestInfo(), true);
	   return new PgDetailResponse(responseInfo, decyrptList);
   }
	
	
	
	
//	/**
//	 * Service layer for updating billing slabs
//	 * @param billingSlabReq
//	 * @return
//	 */
//	public BillingSlabRes updateSlabs(BillingSlabReq billingSlabReq) {
//		enrichSlabsForUpdate(billingSlabReq);
//		billingSlabReq.getBillingSlab().parallelStream().forEach(slab -> {
//			List<BillingSlab> slabs = new ArrayList<>();
//			slabs.add(slab);
//			BillingSlabReq req = BillingSlabReq.builder().requestInfo(billingSlabReq.getRequestInfo()).billingSlab(slabs).build();
//			producer.push(billingSlabConfigs.getPersisterUpdateTopic(), req);
//		});
//		return BillingSlabRes.builder().responseInfo(factory.createResponseInfoFromRequestInfo(billingSlabReq.getRequestInfo(), true))
//				.billingSlab(billingSlabReq.getBillingSlab()).build();
//	}
//	
//	/**
//	 * Service layer for searching billing slabs from the db
//	 * @param criteria
//	 * @param requestInfo
//	 * @return
//	 */
//	public BillingSlabRes searchSlabs(BillingSlabSearchCriteria criteria, RequestInfo requestInfo) {
//		List<Object> preparedStmtList = new ArrayList<>();
//		String query = queryBuilder.getSearchQuery(criteria, preparedStmtList);
//		return BillingSlabRes.builder().responseInfo(factory.createResponseInfoFromRequestInfo(requestInfo, true))
//				.billingSlab(repository.getDataFromDB(query, preparedStmtList)).build();
//	}
//	
//	/**
//	 * Enriches the request for creating billing slabs. Enrichment includes:
//	 * 1. Preparing audit information for the slab
//	 * 2. Setting id to the billing slabs
//	 * @param billingSlabReq
//	 */
//	public void enrichSlabsForCreate(BillingSlabReq billingSlabReq) {
//		AuditDetails audit = AuditDetails.builder().createdBy(billingSlabReq.getRequestInfo().getUserInfo().getUuid())
//				.createdTime(new Date().getTime()).lastModifiedBy(billingSlabReq.getRequestInfo().getUserInfo().getUuid()).lastModifiedTime(new Date().getTime()).build();
//		for(BillingSlab slab: billingSlabReq.getBillingSlab()) {
//			slab.setId(UUID.randomUUID().toString());
//			slab.setAuditDetails(audit);
//		}
//	}
//	
//	/**
//	 * Enriches the request for updating billing slabs. Enrichment includes:
//	 * 1. Preparing audit information for the slab
//	 * @param billingSlabReq
//	 */
//	public void enrichSlabsForUpdate(BillingSlabReq billingSlabReq) {
//		AuditDetails audit = AuditDetails.builder().lastModifiedBy(billingSlabReq.getRequestInfo().getUserInfo().getUuid()).lastModifiedTime(new Date().getTime()).build();
//		billingSlabReq.getBillingSlab().parallelStream().forEach(slab ->  slab.setAuditDetails(audit) );
//	}
//	
//	
//	/**
//	 * Gets MDMS data from the mdms service based on the masters required.
//	 * @param billingSlabReq
//	 * @return Map<String, List<String>>
//	 */
//	public Map<String, List<String>> getMDMSDataForValidation(BillingSlabReq billingSlabReq){
//		Map<String, List<String>> mdmsMap = new HashMap<>();
//		String[] masters = {BillingslabConstants.TL_MDMS_TRADETYPE, BillingslabConstants.TL_MDMS_ACCESSORIESCATEGORY, 
//				BillingslabConstants.TL_MDMS_STRUCTURETYPE, BillingslabConstants.TL_MDMS_UOM};
//		for(String master: Arrays.asList(masters)) {
//			StringBuilder uri = new StringBuilder();
//			String module = BillingslabConstants.TL_MDMS_MODULE_NAME;
//			if(master.equals(BillingslabConstants.TL_MDMS_STRUCTURETYPE) || master.equals(BillingslabConstants.TL_MDMS_UOM))
//				module = BillingslabConstants.COMMON_MASTERS_MDMS_MODULE_NAME;
//			MdmsCriteriaReq request = util.prepareMDMSSearchReq(uri, billingSlabReq.getBillingSlab().get(0).getTenantId(), module, master, null, billingSlabReq.getRequestInfo());
//			try {
//				Object response = restTemplate.postForObject(uri.toString(), request, Map.class);
//				if(null != response) {
//					String jsonPath = BillingslabConstants.MDMS_JSONPATH_FOR_MASTER_CODES.replaceAll("#module#", module).replaceAll("#master#", master);
//					List<String> data = JsonPath.read(response, jsonPath);
//					mdmsMap.put(master, data);
//				}
//			}catch(Exception e) {
//				log.error("Couldn't fetch master: "+master);
//				log.error("Exception: "+e);
//				mdmsMap.put(master, new ArrayList<>());
//				continue;
//			}
//			
//		}
//		
//		return mdmsMap;
//	}

}

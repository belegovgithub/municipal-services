package org.egov.wscalculation.service;

import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.wscalculation.web.models.BillEstimation;
import org.egov.wscalculation.web.models.Calculation;
import org.egov.wscalculation.web.models.CalculationReq;

public interface WSCalculationService {

	Map<String,Object> getCalculation(CalculationReq calculationReq);

	void jobScheduler();

	void generateDemandBasedOnTimePeriod(RequestInfo requestInfo);
	
	public void generateDemandBasedOnTimePeriod_manual(RequestInfo requestInfo,String tenantId, List<String> connectionnos);
	
	public void checkFailedBills(RequestInfo requestInfo,Long fromDateSearch , Long toDateSearch , String tenantId, String connectionno);

	public void generateDemandForNewModifiedConn(RequestInfo requestInfo);
	
	public BillEstimation getBillEstimate(CalculationReq request);
	 
}

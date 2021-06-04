package org.egov.wscalculation.repository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.egov.wscalculation.web.models.BillFailureNotificationObj;
import org.egov.wscalculation.web.models.MeterConnectionRequest;
import org.egov.wscalculation.web.models.MeterReading;
import org.egov.wscalculation.web.models.MeterReadingSearchCriteria;

public interface WSCalculationDao {

	void saveMeterReading(MeterConnectionRequest meterConnectionRequest);
	
	List<MeterReading> searchMeterReadings(MeterReadingSearchCriteria criteria);
	
	ArrayList<String> searchTenantIds();

	ArrayList<String> searchConnectionNos(String connectionType, String tenantId);
	
	List<MeterReading> searchCurrentMeterReadings(MeterReadingSearchCriteria criteria);
	
	int isMeterReadingConnectionExist(List<String> ids);
	
	List<String> getConnectionsNoList(String tenantId, String connectionType);
	
	List<BillFailureNotificationObj>  getFailedBillDtl(String tenantId,String connectionType);
	
	List<String> getTenantId();
	
	int isBillingPeriodExists(String connectionNo, String billingPeriod);
 

	List<String> getConnectionsNoList(String tenantId, String connectionType, Calendar activationDate);

}

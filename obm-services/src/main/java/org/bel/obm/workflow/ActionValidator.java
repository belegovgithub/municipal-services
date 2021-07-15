package org.bel.obm.workflow;

import java.util.HashMap;
import java.util.Map;

import org.bel.obm.constants.OBMConstant;
import org.bel.obm.models.CHBookDtls;
import org.bel.obm.models.CHBookRequest;
import org.bel.obm.workflow.models.BusinessService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class ActionValidator {

	@Autowired
	private WorkflowService workflowService;

	public void validateUpdateRequest(CHBookRequest request, BusinessService businessService) {
		validateDocumentsForUpdate(request);
		validateIds(request, businessService);
	}

	private void validateDocumentsForUpdate(CHBookRequest request) {
		Map<String, String> errorMap = new HashMap<>();
		if (OBMConstant.ACTION_APPLY.equalsIgnoreCase(request.getBooking().getAction())) {
			if (request.getBooking().getApplicationDocuments() == null)
				errorMap.put("INVALID STATUS", "Status cannot be APPLY when application document are not provided");
		}

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	private void validateIds(CHBookRequest request, BusinessService businessService) {
		Map<String, String> errorMap = new HashMap<>();
		CHBookDtls chBookDtls = request.getBooking();
		String namefBusinessService = chBookDtls.getBusinessService();
		if ((namefBusinessService == null) || (namefBusinessService.equals(OBMConstant.businessService_OBM))) {
			if (!workflowService.isStateUpdatable(chBookDtls.getStatus(), businessService)) {
				if (chBookDtls.getId() == null)
					errorMap.put("INVALID UPDATE", "Id of CH Booking cannot be null");
				if (chBookDtls.getBankDetails().getId() == null)
					errorMap.put("INVALID UPDATE", "Id of BankDetail cannot be null");
				if (!CollectionUtils.isEmpty(chBookDtls.getApplicationDocuments())) {
					chBookDtls.getApplicationDocuments().forEach(document -> {
						if (document.getId() == null)
							errorMap.put("INVALID UPDATE", "Id of applicationDocument cannot be null");
					});
				}
			}
		}
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

}

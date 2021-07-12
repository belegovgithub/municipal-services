package org.bel.obm.validators;

import java.util.HashMap;
import java.util.Map;

import org.bel.obm.constants.OBMConstant;
import org.bel.obm.models.CHBookDtls;
import org.bel.obm.models.CHBookRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

@Component
public class CHBookValidator {

	public void validateFields(CHBookRequest request) {
		Map<String, String> errorMap = new HashMap<>();
		if (null == request.getCHBookDtls().getHallId() || request.getCHBookDtls().getHallId().isEmpty())
			errorMap.put("NULL_HallDtl", " Hall id cannot be empty");
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	public void validateBusinessService(CHBookRequest request) {
		if (!request.getCHBookDtls().getBusinessService().equalsIgnoreCase(OBMConstant.businessService_OBM))
			throw new CustomException("BUSINESSSERVICE_NOTMATCHING", " The business service not matching ");
	}

	public void validateUserwithOwnerDetail(RequestInfo request, CHBookDtls bookDtls) {
		Map<String, String> errorMap = new HashMap<>();
		if (request.getUserInfo().getType().equals(OBMConstant.ROLE_CITIZEN)) {
			String uuid = request.getUserInfo().getUuid();

			if (!bookDtls.getAuditDetails().getCreatedBy().equals(uuid)) {
				errorMap.put("UNAUTHORIZED USER",
						"Unauthorized user to access the application:  " + bookDtls.getApplicationNumber());
			}
		}
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}
}

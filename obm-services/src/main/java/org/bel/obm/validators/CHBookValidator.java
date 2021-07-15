package org.bel.obm.validators;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bel.obm.constants.OBMConstant;
import org.bel.obm.models.CHBookDtls;
import org.bel.obm.models.CHBookRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class CHBookValidator {

	public void validateFields(CHBookRequest request) {
		Map<String, String> errorMap = new HashMap<>();
		if (null == request.getBooking().getHallId() || request.getBooking().getHallId().isEmpty())
			errorMap.put("NULL_HallDtl", " Hall id cannot be empty");
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	public void validateBusinessService(CHBookRequest request) {
		if (!request.getBooking().getBusinessService().equalsIgnoreCase(OBMConstant.businessService_OBM))
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

	public void validateUpdate(CHBookRequest request, List<CHBookDtls> searchResult) {
		if (searchResult.size() != 1)
			throw new CustomException("INVALID UPDATE", "The data to be updated is not in database");
		validateAllIds(searchResult, request.getBooking());
		validateDuplicateDocuments(request);
	}

	private void validateAllIds(List<CHBookDtls> searchResult, CHBookDtls chBookDtls) {

		Map<String, CHBookDtls> idToChBookFromSearch = new HashMap<>();
		searchResult.forEach(chBookDtl -> {
			idToChBookFromSearch.put(chBookDtl.getId(), chBookDtl);
		});

		Map<String, String> errorMap = new HashMap<>();
		CHBookDtls searchedChBook = idToChBookFromSearch.get(chBookDtls.getId());

		if (!searchedChBook.getApplicationNumber().equalsIgnoreCase(chBookDtls.getApplicationNumber()))
			errorMap.put("INVALID UPDATE",
					"The application number from search: " + searchedChBook.getApplicationNumber()
							+ " and from update: " + chBookDtls.getApplicationNumber() + " does not match");

		if (!searchedChBook.getBankDetails().getId().equalsIgnoreCase(chBookDtls.getBankDetails().getId()))
			errorMap.put("INVALID UPDATE", "The id " + chBookDtls.getBankDetails().getId() + " does not exist");

		compareIdList(getApplicationDocIds(searchedChBook), getApplicationDocIds(chBookDtls), errorMap);

		if (!CollectionUtils.isEmpty(errorMap))
			throw new CustomException(errorMap);
	}

	private List<String> getApplicationDocIds(CHBookDtls searchedChBook) {
		List<String> applicationDocIds = new LinkedList<>();
		if (!CollectionUtils.isEmpty(searchedChBook.getApplicationDocuments())) {
			searchedChBook.getApplicationDocuments().forEach(document -> {
				applicationDocIds.add(document.getId());
			});
		}
		return applicationDocIds;
	}

	private void compareIdList(List<String> searchIds, List<String> updateIds, Map<String, String> errorMap) {
		if (!CollectionUtils.isEmpty(searchIds))
			searchIds.forEach(searchId -> {
				if (!updateIds.contains(searchId))
					errorMap.put("INVALID UPDATE", "The id: " + searchId + " was not present in update request");
			});
	}

	private void validateDuplicateDocuments(CHBookRequest request) {
		List<String> documentFileStoreIds = new LinkedList();
		if (!CollectionUtils.isEmpty(request.getBooking().getApplicationDocuments())) {
			request.getBooking().getApplicationDocuments().forEach(document -> {
				if (documentFileStoreIds.contains(document.getFileStoreId()))
					throw new CustomException("DUPLICATE_DOCUMENT ERROR",
							"Same document cannot be used multiple times");
				else
					documentFileStoreIds.add(document.getFileStoreId());
			});
		}
	}
}

package org.bel.obm.validators;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.bel.obm.constants.OBMConstant;
import org.bel.obm.models.CHBookDtls;
import org.bel.obm.models.CHBookRequest;
import org.bel.obm.util.CommonUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.DocumentContext;

@Component
public class CHBookValidator {

	@Autowired
	private CommonUtils util;

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

	public void validateAndEncrichMDMSData(CHBookRequest request) {
		Map<String, String> errorMap = new HashMap<>();
		try {

			CHBookDtls chBookDtls = request.getBooking();
			MdmsCriteriaReq mdmsReqCHB = util.prepareMdMsRequest(chBookDtls.getTenantId(), "CommunityHallBooking",
					Arrays.asList("CommunityHalls"), "[?(@.hallCode == '" + chBookDtls.getHallId() + "')]",
					request.getRequestInfo());
			DocumentContext mdmsDataCHB = util.getAttributeValues(mdmsReqCHB);

			List<String> cHBHallIds = mdmsDataCHB.read("$.MdmsRes.CommunityHallBooking.CommunityHalls.*.hallCode");
			if (cHBHallIds.size() >= 1) {

				List<HashMap<String, String>> cHBResidentTypes = mdmsDataCHB
						.read("$.MdmsRes.CommunityHallBooking.CommunityHalls.*.residentType[?(@.type == '"
								+ chBookDtls.getResidentTypeId() + "')]");
				if (cHBResidentTypes.size() == 0)
					errorMap.put("INVALID_ID", "Invalid Resident type");

				List<HashMap<String, String>> cHBPurposes = mdmsDataCHB
						.read("$.MdmsRes.CommunityHallBooking.CommunityHalls.*.purposes[?(@.purpose == '"
								+ chBookDtls.getPurpose() + "')]");
				if (cHBPurposes.size() == 0)
					errorMap.put("INVALID_ID", "Invalid Purpose");

				List<HashMap<String, String>> cHBCategories = mdmsDataCHB
						.read("$.MdmsRes.CommunityHallBooking.CommunityHalls.*.specialCategories[?(@.category == '"
								+ chBookDtls.getCategory() + "')]");
				if (cHBCategories.size() == 0)
					errorMap.put("INVALID_ID", "Invalid Category");

				List<HashMap<String, String>> chbTimeSlotsIds = mdmsDataCHB
						.read("$.MdmsRes.CommunityHallBooking.CommunityHalls.*.timeSlots[?(@.id == '"
								+ chBookDtls.getTimeSlotId() + "')]");
				if (chbTimeSlotsIds.size() == 0)
					errorMap.put("INVALID_ID", "Invalid Timeslot id");
				for (HashMap<String, String> record : chbTimeSlotsIds) {
					String startTime = record.get("from");
					String duration = record.get("duration");
					SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
					sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
					chBookDtls.setToDate(chBookDtls.getSelectedDate() + sdf.parse(startTime).getTime()
							+ sdf.parse(duration).getTime());
					chBookDtls.setFromDate(chBookDtls.getSelectedDate() + sdf.parse(startTime).getTime());
				}
			} else
				errorMap.put("INVALID_ID", "Invalid hall id");
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException("Invalid_Data", "Invalid MDMS Data");
		}
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}
}

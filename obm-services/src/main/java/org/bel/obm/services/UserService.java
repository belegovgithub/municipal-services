package org.bel.obm.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bel.obm.constants.OBMConstant;
import org.bel.obm.models.CHBookDtls;
import org.bel.obm.models.CHBookRequest;
import org.bel.obm.models.SearchCriteria;
import org.bel.obm.repository.ServiceRequestRepository;
import org.bel.obm.user.CreateUserRequest;
import org.bel.obm.user.UserDetailResponse;
import org.bel.obm.user.UserSearchRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserService {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Value("${egov.user.host}")
	private String userHost;

	@Value("${egov.user.context.path}")
	private String userContextPath;

	@Value("${egov.user.create.path}")
	private String userCreateEndpoint;

	@Value("${egov.user.search.path}")
	private String userSearchEndpoint;

	@Value("${egov.user.update.path}")
	private String userUpdateEndpoint;

	public void createUser(CHBookRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		List<String> roles = requestInfo.getUserInfo().getRoles().stream().map(Role::getCode)
				.collect(Collectors.toList());
		CHBookDtls chBookDtls = request.getCHBookDtls();
		if (roles.contains("LR_CEMP")) {
			if (chBookDtls.getUserDetails().getUuid() == null) {
				String existingUuid = isUserPresent(chBookDtls.getUserDetails(), requestInfo, chBookDtls.getTenantId());
				if (existingUuid.equalsIgnoreCase("null")) {
					chBookDtls.setAccountId(
							createUser(chBookDtls.getUserDetails(), requestInfo, chBookDtls.getTenantId()));
				} else {
					chBookDtls.setAccountId(existingUuid);
				}
			} else
				chBookDtls.setAccountId(chBookDtls.getUserDetails().getUuid());
		} else if (requestInfo.getUserInfo().getType().equalsIgnoreCase(OBMConstant.ROLE_CITIZEN)) {
			chBookDtls.setUserDetails(requestInfo.getUserInfo());
		}
	}

	private String createUser(User userInfo, RequestInfo requestInfo, String tenantId) {
		userInfo.setUserName(UUID.randomUUID().toString());
		userInfo.setTenantId(tenantId.split("\\.")[0]);
		userInfo.setType(OBMConstant.ROLE_CITIZEN);
		userInfo.setRoles(
				Collections.singletonList(Role.builder().code(OBMConstant.ROLE_CITIZEN).name("Citizen").build()));
		StringBuilder uri = new StringBuilder(userHost).append(userContextPath).append(userCreateEndpoint);
		UserDetailResponse userDetailResponse = userCall(new CreateUserRequest(requestInfo, userInfo), uri);
		if (userDetailResponse.getUser().get(0).getUuid() == null) {
			throw new CustomException("INVALID USER RESPONSE", "The user created has uuid as null");
		}
		return userDetailResponse.getUser().get(0).getUuid().toString();
	}

	@SuppressWarnings("unchecked")
	private UserDetailResponse userCall(Object userRequest, StringBuilder url) {

		String dobFormat = null;
		if (url.indexOf(userSearchEndpoint) != -1 || url.indexOf(userUpdateEndpoint) != -1)
			dobFormat = "yyyy-MM-dd";
		else if (url.indexOf(userCreateEndpoint) != -1)
			dobFormat = "dd/MM/yyyy";
		try {
			LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(url, userRequest);
			parseResponse(responseMap, dobFormat);
			UserDetailResponse userDetailResponse = mapper.convertValue(responseMap, UserDetailResponse.class);
			return userDetailResponse;
		} catch (IllegalArgumentException e) {
			throw new CustomException("IllegalArgumentException", "ObjectMapper not able to convertValue in userCall");
		}
	}

	@SuppressWarnings("unchecked")
	private void parseResponse(LinkedHashMap<String, Object> responeMap, String dobFormat) {
		List<LinkedHashMap<String, Object>> users = (List<LinkedHashMap<String, Object>>) responeMap.get("user");
		String format1 = "dd-MM-yyyy HH:mm:ss";

		if (null != users) {

			users.forEach(map -> {

				map.put("createdDate", dateTolong((String) map.get("createdDate"), format1));
				if ((String) map.get("lastModifiedDate") != null)
					map.put("lastModifiedDate", dateTolong((String) map.get("lastModifiedDate"), format1));
				if ((String) map.get("dob") != null)
					map.put("dob", dateTolong((String) map.get("dob"), dobFormat));
				if ((String) map.get("pwdExpiryDate") != null)
					map.put("pwdExpiryDate", dateTolong((String) map.get("pwdExpiryDate"), format1));
			});
		}
	}

	private Long dateTolong(String date, String format) {
		SimpleDateFormat f = new SimpleDateFormat(format);
		Date d = null;
		try {
			d = f.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d.getTime();
	}

	private String isUserPresent(User userInfo, RequestInfo requestInfo, String tenantId) {
		UserSearchRequest searchRequest = UserSearchRequest.builder().userName(userInfo.getMobileNumber())
				.tenantId(tenantId).build();
		return getUserCommon(searchRequest, requestInfo).getUser().get(0).getUuid().toString();
	}

	public UserDetailResponse getUser(SearchCriteria criteria, RequestInfo requestInfo) {
		if (null != criteria.getTenantId()) {
			UserSearchRequest userSearchRequest = UserSearchRequest.builder()
					.tenantId(criteria.getTenantId()).mobileNumber(criteria.getMobileNumber()).active(true)
					.build();
			return getUserCommon(userSearchRequest, requestInfo);
		} else if (null != criteria.getTenantIds() && criteria.getTenantIds().size() > 0) {
			for (String tenantId : criteria.getTenantIds()) {
				UserSearchRequest userSearchRequest = UserSearchRequest.builder()
						.tenantId(tenantId).mobileNumber(criteria.getMobileNumber()).active(true)
						.build();
				return getUserCommon(userSearchRequest, requestInfo);
			}
		}
		return null;
	}

	public UserDetailResponse getUserByUUid(String accountId, RequestInfo requestInfo) {
		UserSearchRequest userSearchRequest = UserSearchRequest.builder()
				.uuid(Arrays.asList(accountId)).build();
		return getUserCommon(userSearchRequest, requestInfo);
	}

	public UserDetailResponse getUserCommon(UserSearchRequest userSearchRequest , RequestInfo requestInfo) {
		userSearchRequest.setUserType(OBMConstant.ROLE_CITIZEN);
		userSearchRequest.setRequestInfo(requestInfo);
		StringBuilder url = new StringBuilder(userHost + userSearchEndpoint);
		UserDetailResponse res = mapper.convertValue(serviceRequestRepository.fetchResult(url, userSearchRequest),
				UserDetailResponse.class);
		return res;
	}
}

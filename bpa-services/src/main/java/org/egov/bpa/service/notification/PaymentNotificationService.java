package org.egov.bpa.service.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.service.BPAService;
import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.util.NotificationUtil;
import org.egov.bpa.web.models.BPA;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.web.models.BPASearchCriteria;
import org.egov.bpa.web.models.EventRequest;
import org.egov.bpa.web.models.SMSRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;

@Service
public class PaymentNotificationService {

	private BPAConfiguration config;

	private BPAService bpaService;

	private NotificationUtil util;

	private ObjectMapper mapper;

	private BPANotificationService bpaNotificationService;

	@Autowired
	public PaymentNotificationService(BPAConfiguration config, BPAService bpaService, NotificationUtil util,
			ObjectMapper mapper, BPANotificationService bpaNotificationService) {
		this.config = config;
		this.bpaService = bpaService;
		this.util = util;
		this.mapper = mapper;
		this.bpaNotificationService = bpaNotificationService;
	}

	final String tenantIdKey = "tenantId";

	final String businessServiceKey = "businessService";

	final String consumerCodeKey = "consumerCode";

	final String payerMobileNumberKey = "mobileNumber";

	final String paidByKey = "paidBy";

	final String amountPaidKey = "amountPaid";

	final String receiptNumberKey = "receiptNumber";

	/**
	 * Generates sms from the input record and Sends smsRequest to SMSService
	 * 
	 * @param record
	 *            The kafka message from receipt create topic
	 */
	/**
	 * Generates sms from the input record and Sends smsRequest to SMSService
	 * 
	 * @param record
	 *            The kafka message from receipt create topic
	 */
	@SuppressWarnings("rawtypes")
	public void process(HashMap<String, Object> record) {
		try {
			String jsonString = new JSONObject(record).toString();
			DocumentContext documentContext = JsonPath.parse(jsonString);
			Map<String, String> valMap = enrichValMap(documentContext);
			if (!StringUtils.equals(BPAConstants.APPL_FEE, valMap.get(businessServiceKey)))
				return;
			if (!StringUtils.equals(BPAConstants.SANC_FEE, valMap.get(businessServiceKey)))
				return;
			Map<String, Object> info = documentContext.read("$.RequestInfo");
			RequestInfo requestInfo = mapper.convertValue(info, RequestInfo.class);

			if (config.getBusinessService().contains(valMap.get(businessServiceKey))) {
				BPA bpa = getBPAFromConsumerCode(valMap.get(tenantIdKey), valMap.get(consumerCodeKey), requestInfo,
						valMap.get(businessServiceKey));

				Map<String, String> mobileNumberToOwner = new HashMap<>();

				String message = "Dear <1>, The payment for you application with the application no as: "
						+ bpa.getApplicationNo() + " is done Successfully. Waiting for Docverification.";
				bpa.getOwners().forEach(owner -> {
					if (owner.getMobileNumber() != null)
						mobileNumberToOwner.put(owner.getMobileNumber(), owner.getName());
				});
				List<SMSRequest> smsList = new ArrayList<>();
				List<Map> users = new ArrayList<Map>();
				users.add(mobileNumberToOwner);
				smsList.addAll(util.createSMSRequest(message, mobileNumberToOwner));
				util.sendSMS(smsList, config.getIsSMSEnabled());

				if (null != config.getIsUserEventsNotificationEnabled()) {
					if (config.getIsUserEventsNotificationEnabled()) {
						BPARequest bpaRequest = BPARequest.builder().requestInfo(requestInfo).BPA(bpa).build();
						EventRequest eventRequest = bpaNotificationService.getEvents(bpaRequest);
						if (null != eventRequest)
							util.sendEventNotification(eventRequest);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Enriches the map with values from receipt
	 * 
	 * @param context
	 *            The documentContext of the receipt
	 * @return The map containing required fields from receipt
	 */
	private Map<String, String> enrichValMap(DocumentContext context) {
		Map<String, String> valMap = new HashMap<>();
		try {
			valMap.put(businessServiceKey,
					(String) ((JSONArray) context
							.read("$.Payment.paymentDetails[?(@.businessService=='BPA.NC_APP_FEE')].businessService"))
									.get(0));
			valMap.put(consumerCodeKey,
					(String) ((JSONArray) context
							.read("$.Payment.paymentDetails[?(@.businessService=='BPA.NC_APP_FEE')].bill.consumerCode"))
									.get(0));
			valMap.put(tenantIdKey, context.read("$.Payment.tenantId"));
			valMap.put(payerMobileNumberKey,
					(String) ((JSONArray) context
							.read("$.Payment.paymentDetails[?(@.businessService=='BPA.NC_APP_FEE')].bill.mobileNumber"))
									.get(0));
			valMap.put(paidByKey, context.read("$.Payment.paidBy"));
			Integer amountPaid = (Integer) ((JSONArray) context
					.read("$.Payment.paymentDetails[?(@.businessService=='BPA.NC_APP_FEE')].bill.amountPaid")).get(0);
			valMap.put(amountPaidKey, amountPaid.toString());
			valMap.put(receiptNumberKey,
					(String) ((JSONArray) context
							.read("$.Payment.paymentDetails[?(@.businessService=='BPA.NC_APP_FEE')].receiptNumber"))
									.get(0));

		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException("RECEIPT ERROR", "Unable to fetch values from receipt");
		}
		return valMap;
	}

	/**
	 * Searches the tradeLicense based on the consumer code as applicationNumber
	 * 
	 * @param tenantId
	 *            tenantId of the tradeLicense
	 * @param consumerCode
	 *            The consumerCode of the receipt
	 * @param requestInfo
	 *            The requestInfo of the request
	 * @return TradeLicense for the particular consumerCode
	 */
	private BPA getBPAFromConsumerCode(String tenantId, String consumerCode, RequestInfo requestInfo,
			String businessService) {

		BPASearchCriteria searchCriteria = new BPASearchCriteria();
		List<String> codes = Arrays.asList(consumerCode);
		searchCriteria.setApplicationNos(codes);
		searchCriteria.setTenantId(tenantId);
		List<BPA> bpas = bpaService.getBPAWithOwnerInfo(searchCriteria, requestInfo);

		if (CollectionUtils.isEmpty(bpas))
			throw new CustomException("INVALID RECEIPT",
					"No Appllication found for the consumerCode: " + consumerCode + " and tenantId: " + tenantId);

		if (bpas.size() != 1)
			throw new CustomException("INVALID RECEIPT",
					"Multiple Application found for the consumerCode: " + consumerCode + " and tenantId: " + tenantId);

		return bpas.get(0);
	}
}

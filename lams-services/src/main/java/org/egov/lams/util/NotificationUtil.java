package org.egov.lams.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.lams.config.LamsConfiguration;
import org.egov.lams.model.SMSRequest;
import org.egov.lams.producer.Producer;
import org.egov.lams.repository.ServiceRequestRepository;
import org.egov.lams.web.models.LeaseAgreementRenewal;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NotificationUtil {

	private LamsConfiguration config;

	private ServiceRequestRepository serviceRequestRepository;

	private Producer producer;

	private RestTemplate restTemplate;

	@Autowired
	public NotificationUtil(LamsConfiguration config, ServiceRequestRepository serviceRequestRepository, Producer producer, RestTemplate restTemplate) {
		this.config = config;
		this.serviceRequestRepository = serviceRequestRepository;
		this.producer = producer;
		this.restTemplate = restTemplate;
	}

	public String getLocalizationMessages(String tenantId, RequestInfo requestInfo) {
		LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(getUri(tenantId, requestInfo),
				requestInfo);
		String jsonString = new JSONObject(responseMap).toString();
		return jsonString;
	}

	public SMSRequest getCustomizedMsg(RequestInfo requestInfo, LeaseAgreementRenewal leaseRenewal, String localizationMessage) {
		SMSRequest messageTemplate = null;
		String ACTION_STATUS = leaseRenewal.getAction() + "_" + leaseRenewal.getStatus();
		switch (ACTION_STATUS) {
		case LRConstants.ACTION_STATUS_APPLIED:
			messageTemplate = getMessageTemplate(LRConstants.NOTIFICATION_APPLIED, localizationMessage);
			messageTemplate.setMessage(getAppliedMsg(leaseRenewal, messageTemplate.getMessage()));
			break;
		case LRConstants.ACTION_STATUS_CITIZENREVIEW:
			messageTemplate = getMessageTemplate(LRConstants.NOTIFICATION_CITIZENREVIEW, localizationMessage);
			messageTemplate.setMessage(getOtherMsg(leaseRenewal, messageTemplate.getMessage()));
			break;
		case LRConstants.ACTION_STATUS_APPROVED:
			messageTemplate = getMessageTemplate(LRConstants.NOTIFICATION_APPROVED, localizationMessage);
			messageTemplate.setMessage(getOtherMsg(leaseRenewal, messageTemplate.getMessage()));
			break;
		case LRConstants.ACTION_STATUS_REJECTED:
			messageTemplate = getMessageTemplate(LRConstants.NOTIFICATION_REJECTED, localizationMessage);
			messageTemplate.setMessage(getOtherMsg(leaseRenewal, messageTemplate.getMessage()));
			break;
		case LRConstants.ACTION_STATUS_CEOEXAMINATION:
			messageTemplate = getMessageTemplate(LRConstants.NOTIFICATION_CEOEXAMINATION, localizationMessage);
			messageTemplate.setMessage(getOtherMsg(leaseRenewal, messageTemplate.getMessage()));
			break;
		case LRConstants.ACTION_STATUS_DEOEXAMINATION:
			messageTemplate = getMessageTemplate(LRConstants.NOTIFICATION_DEOEXAMINATION, localizationMessage);
			messageTemplate.setMessage(getOtherMsg(leaseRenewal, messageTemplate.getMessage()));
			break;
		case LRConstants.ACTION_STATUS_PDDEEXAMINATION:
			messageTemplate = getMessageTemplate(LRConstants.NOTIFICATION_PDDEEXAMINATION, localizationMessage);
			messageTemplate.setMessage(getOtherMsg(leaseRenewal, messageTemplate.getMessage()));
			break;
		case LRConstants.ACTION_STATUS_DGDEEXAMINATION:
			messageTemplate = getMessageTemplate(LRConstants.NOTIFICATION_DGDEEXAMINATION, localizationMessage);
			messageTemplate.setMessage(getOtherMsg(leaseRenewal, messageTemplate.getMessage()));
			break;
		case LRConstants.ACTION_STATUS_MODEXAMINATION:
			messageTemplate = getMessageTemplate(LRConstants.NOTIFICATION_MODEXAMINATION, localizationMessage);
			messageTemplate.setMessage(getOtherMsg(leaseRenewal, messageTemplate.getMessage()));
			break;
		}
		return messageTemplate;
	}

	private SMSRequest getMessageTemplate(String notificationCode, String localizationMessage) {
		String path = "$..messages[?(@.code==\"{}\")].message";
		path = path.replace("{}", notificationCode);
		String message = null;
		
		String pathTemplate = "$..messages[?(@.code==\"{}\")].templateId";
		pathTemplate = pathTemplate.replace("{}", notificationCode);
		String templateId = null;
		
		try {
			Object messageObj = JsonPath.parse(localizationMessage).read(path);
			message = ((ArrayList<String>) messageObj).get(0);
			
			Object templateObj = JsonPath.parse(localizationMessage).read(pathTemplate);
			templateId = ((ArrayList<String>) templateObj).get(0);
		} catch (Exception e) {
			log.warn("Fetching from localization failed", e);
		}
		SMSRequest smsRequest = new SMSRequest();
		smsRequest.setMessage(message);
		smsRequest.setTemplateId(templateId);
		return smsRequest;
	}


	private String getOtherMsg(LeaseAgreementRenewal lease, String message) {
		message = message.replace("<1>", String.valueOf(lease.getApplicationType()).toLowerCase());
		message = message.replace("<2>", lease.getApplicationNumber());

		return message;
	}

	private String getAppliedMsg(LeaseAgreementRenewal lease, String message) {
		message = message.replace("<1>", String.valueOf(lease.getApplicationType()).toLowerCase());
		message = message.replace("<2>", lease.getLeaseDetails().getSurveyNo());
		message = message.replace("<3>", lease.getApplicationNumber());

		return message;
	}
	
	public void sendSMS(List<SMSRequest> smsRequestList, boolean isSMSEnabled) {
		if (isSMSEnabled) {
			if (CollectionUtils.isEmpty(smsRequestList))
				log.info("Messages from localization couldn't be fetched!");
			for (SMSRequest smsRequest : smsRequestList) {
				producer.push(config.getSmsNotifTopic(), smsRequest);
				log.info("MobileNumber: " + smsRequest.getMobileNumber() + " Messages: " + smsRequest.getMessage());
			}
		}
	}

	public List<SMSRequest> createSMSRequest(String message, Map<String, String> mobileNumberToOwnerName , String templateId) {
		List<SMSRequest> smsRequest = new LinkedList<>();
		for (Map.Entry<String, String> entryset : mobileNumberToOwnerName.entrySet()) {
			String customizedMsg = message.replace("<1>", entryset.getValue());
			customizedMsg = customizedMsg.replace(LRConstants.NOTIF_OWNER_NAME_KEY, entryset.getValue());
			smsRequest.add(new SMSRequest(entryset.getKey(), customizedMsg, templateId));
		}
		return smsRequest;
	}
	
	public StringBuilder getUri(String tenantId, RequestInfo requestInfo) {

		if (config.getIsLocalizationStateLevel())
			tenantId = tenantId.split("\\.")[0];

		String locale = LRConstants.NOTIFICATION_LOCALE;
		if (!StringUtils.isEmpty(requestInfo.getMsgId()) && requestInfo.getMsgId().split("|").length >= 2)
			locale = requestInfo.getMsgId().split("\\|")[1];

		StringBuilder uri = new StringBuilder();
		uri.append(config.getLocalizationHost()).append(config.getLocalizationContextPath())
				.append(config.getLocalizationSearchEndpoint()).append("?").append("locale=").append(locale)
				.append("&tenantId=").append(tenantId).append("&module=").append(LRConstants.MODULE)
				.append("&codes=").append(StringUtils.join(LRConstants.NOTIFICATION_CODES,','));

		return uri;
	}

}
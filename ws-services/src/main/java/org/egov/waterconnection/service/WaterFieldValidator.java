package org.egov.waterconnection.service;

import org.egov.waterconnection.constants.WCConstants;
import org.egov.waterconnection.web.models.ValidatorResult;
import org.egov.waterconnection.web.models.WaterConnectionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Component
public class WaterFieldValidator implements WaterActionValidator {

	@Autowired
	private ObjectMapper mapper;
	
	@Override
	public ValidatorResult validate(WaterConnectionRequest waterConnectionRequest, int reqType) {
		Map<String, String> errorMap = new HashMap<>();
		if (reqType == WCConstants.UPDATE_APPLICATION) {
			handleUpdateApplicationRequest(waterConnectionRequest, errorMap);
		}
		if(reqType == WCConstants.MODIFY_CONNECTION){
			handleModifyConnectionRequest(waterConnectionRequest, errorMap);
		}
		if(reqType == WCConstants.DEACTIVATE_CONNECTION){
			handleDeactivateConnectionRequest(waterConnectionRequest, errorMap);
		}
		if (!errorMap.isEmpty())
			return new ValidatorResult(false, errorMap);
		return new ValidatorResult(true, errorMap);
	}

	private void handleUpdateApplicationRequest(WaterConnectionRequest waterConnectionRequest,
			Map<String, String> errorMap) {
		if (WCConstants.ACTIVATE_CONNECTION_CONST
				.equalsIgnoreCase(waterConnectionRequest.getWaterConnection().getProcessInstance().getAction())) {
			if (StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getConnectionType())) {
				errorMap.put("INVALID_WATER_CONNECTION_TYPE", "Connection type should not be empty");
			}
			if (StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getWaterSource())) {
				errorMap.put("INVALID_WATER_SOURCE", "WaterConnection cannot be created  without water source");
			}
			/*
			 * if
			 * (StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getRoadType(
			 * ))) { errorMap.put("INVALID_ROAD_TYPE", "Road type should not be empty"); }
			 */
			if (StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getConnectionExecutionDate()) ||
					waterConnectionRequest.getWaterConnection().getConnectionExecutionDate() .equals(WCConstants.INVALID_CONNECTION_EXECUTION_DATE)) {
				errorMap.put("INVALID_CONNECTION_EXECUTION_DATE", "Connection execution date should not be empty");
			}

		}
		 
	}
	
	private void handleDeactivateConnectionRequest(WaterConnectionRequest waterConnectionRequest,
			Map<String, String> errorMap) {
		if (WCConstants.DEACTIVATE_FREEZE_CONNECTION
				.equalsIgnoreCase(waterConnectionRequest.getWaterConnection().getProcessInstance().getAction())) {
			if (StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getDeactivationDate())||
					waterConnectionRequest.getWaterConnection().getDeactivationDate().equals(WCConstants.INVALID_CONNECTION_EXECUTION_DATE)) {
				errorMap.put("INVALID_CONNECTION_DEACTIVATION_DATE", "Connection Deactivation date should not be empty");
			}
			if (CollectionUtils.isEmpty(waterConnectionRequest.getWaterConnection().getPlumberInfo()))  {
				errorMap.put("INVALID_PLUMBER_INFO", "Plumber Info Missing");
			}
			HashMap<String, Object> additionalDetail = mapper
					.convertValue(waterConnectionRequest.getWaterConnection().getAdditionalDetails(), HashMap.class);
			if(!additionalDetail.isEmpty())
			{
				if(StringUtils.isEmpty(additionalDetail.get(WCConstants.LAST_METER_READING_CONST)))
				{
					errorMap.put("INVALID_LAST_METER_READING", "Last Meter Reading Missing");
				}
			}else
			{
				errorMap.put("INVALID_ADDITIONAL_DETAILS", "Additional Details Missing");
			}
		}
	}
	
	private void handleModifyConnectionRequest(WaterConnectionRequest waterConnectionRequest, Map<String, String> errorMap){
		
		 
		
		if (WCConstants.VERIFY_AND_FORWARD_CONST
				.equalsIgnoreCase(waterConnectionRequest.getWaterConnection().getProcessInstance().getAction()) ||
				WCConstants.APPROVE_CONNECTION_CONST
				.equalsIgnoreCase(waterConnectionRequest.getWaterConnection().getProcessInstance().getAction())	) {
			if (StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getConnectionType())) {
				errorMap.put("INVALID_WATER_CONNECTION_TYPE", "Connection type should not be empty");
			}
			if (StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getWaterSource())) {
				errorMap.put("INVALID_WATER_SOURCE", "WaterConnection cannot be created  without water source");
			}
			if (StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getConnectionExecutionDate()) ||
					waterConnectionRequest.getWaterConnection().getConnectionExecutionDate() .equals(WCConstants.INVALID_CONNECTION_EXECUTION_DATE)) {
				errorMap.put("INVALID_CONNECTION_EXECUTION_DATE", "Connection execution date should not be empty");
			}
		}
		if ( WCConstants.ACTIVATE_CONNECTION.equalsIgnoreCase(
				waterConnectionRequest.getWaterConnection().getProcessInstance().getAction())) {
			if (waterConnectionRequest.getWaterConnection().getDateEffectiveFrom() == null
					|| waterConnectionRequest.getWaterConnection().getDateEffectiveFrom() < 0
					|| waterConnectionRequest.getWaterConnection().getDateEffectiveFrom() == 0) {
				errorMap.put("INVALID_DATE_EFFECTIVE_FROM", "Date effective from cannot be null or negative");
			}
			if (waterConnectionRequest.getWaterConnection().getDateEffectiveFrom() != null) {
				if (System.currentTimeMillis() > waterConnectionRequest.getWaterConnection().getDateEffectiveFrom()) {
					errorMap.put("DATE_EFFECTIVE_FROM_IN_PAST", "Date effective from cannot be past");
				}
				if ((waterConnectionRequest.getWaterConnection().getConnectionExecutionDate() != null)
						&& (waterConnectionRequest.getWaterConnection()
						.getConnectionExecutionDate() > waterConnectionRequest.getWaterConnection()
						.getDateEffectiveFrom())) {

					errorMap.put("DATE_EFFECTIVE_FROM_LESS_THAN_EXCECUTION_DATE",
							"Date effective from cannot be before connection execution date");
				}
				if ((waterConnectionRequest.getWaterConnection().getMeterInstallationDate() != null)
						&& (waterConnectionRequest.getWaterConnection()
						.getMeterInstallationDate() > waterConnectionRequest.getWaterConnection()
						.getDateEffectiveFrom())) {
					errorMap.put("DATE_EFFECTIVE_FROM_LESS_THAN_METER_INSTALLATION_DATE",
							"Date effective from cannot be before meter installation date");
				}

			}
		}
	}
}

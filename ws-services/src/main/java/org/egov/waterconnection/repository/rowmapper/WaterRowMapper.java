package org.egov.waterconnection.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.egov.waterconnection.constants.WCConstants;
import org.egov.waterconnection.web.models.*;
import org.egov.waterconnection.web.models.Connection.StatusEnum;
import org.egov.waterconnection.web.models.workflow.ProcessInstance;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class WaterRowMapper implements ResultSetExtractor<List<WaterConnection>> {
	@Override
    public List<WaterConnection> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, WaterConnection> connectionListMap = new HashMap<>();
        WaterConnection currentWaterConnection = new WaterConnection();
        while (rs.next()) {
            String Id = rs.getString("connection_Id");
            if (connectionListMap.getOrDefault(Id, null) == null) {
                currentWaterConnection = new WaterConnection();
                currentWaterConnection.setTenantId(rs.getString("tenantid"));
                currentWaterConnection.setConnectionCategory(rs.getString("connectionCategory"));
                currentWaterConnection.setConnectionType(rs.getString("connectionType"));
                currentWaterConnection.setWaterSource(rs.getString("waterSource"));
                currentWaterConnection.setSourceInfo(rs.getString("sourceInfo"));
                currentWaterConnection.setMeterId(rs.getString("meterId"));
                currentWaterConnection.setMeterInstallationDate(rs.getLong("meterInstallationDate"));
                currentWaterConnection.setId(rs.getString("connection_Id"));
                currentWaterConnection.setApplicationNo(rs.getString("applicationNo"));
                currentWaterConnection.setApplicationStatus(rs.getString("applicationstatus"));
                currentWaterConnection.setStatus(StatusEnum.fromValue(rs.getString("status")));
                currentWaterConnection.setConnectionNo(rs.getString("connectionNo"));
                currentWaterConnection.setOldConnectionNo(rs.getString("oldConnectionNo"));
                currentWaterConnection.setPipeSize(rs.getDouble("pipeSize"));
                currentWaterConnection.setNoOfTaps(rs.getInt("noOfTaps"));
                currentWaterConnection.setProposedPipeSize(rs.getDouble("proposedPipeSize"));
                currentWaterConnection.setProposedTaps(rs.getInt("proposedTaps"));
                currentWaterConnection.setRoadCuttingArea(rs.getFloat("roadcuttingarea"));
                currentWaterConnection.setRoadType(rs.getString("roadtype"));
                currentWaterConnection.setOldApplication(rs.getBoolean("isoldapplication"));
                currentWaterConnection.setAuthorizedConnection(rs.getString("authorizedconnection"));
                currentWaterConnection.setMotorInfo(rs.getString("motorinfo"));
                currentWaterConnection.setPropertyOwnership(rs.getString("propertyownership"));
                HashMap<String, Object> additionalDetails = new HashMap<>();
                additionalDetails.put(WCConstants.ADHOC_PENALTY, rs.getBigDecimal("adhocpenalty"));
                additionalDetails.put(WCConstants.ADHOC_REBATE, rs.getBigDecimal("adhocrebate"));
                additionalDetails.put(WCConstants.ADHOC_PENALTY_REASON, rs.getString("adhocpenaltyreason"));
                additionalDetails.put(WCConstants.ADHOC_PENALTY_COMMENT, rs.getString("adhocpenaltycomment"));
                additionalDetails.put(WCConstants.ADHOC_REBATE_REASON, rs.getString("adhocrebatereason"));
                additionalDetails.put(WCConstants.ADHOC_REBATE_COMMENT, rs.getString("adhocrebatecomment"));
                additionalDetails.put(WCConstants.INITIAL_METER_READING_CONST, rs.getBigDecimal("initialmeterreading"));
                additionalDetails.put(WCConstants.APP_CREATED_DATE, rs.getBigDecimal("appCreatedDate"));
                additionalDetails.put(WCConstants.DETAILS_PROVIDED_BY, rs.getString("detailsprovidedby"));
                additionalDetails.put(WCConstants.ESTIMATION_FILESTORE_ID, rs.getString("estimationfileStoreId"));
                additionalDetails.put(WCConstants.SANCTION_LETTER_FILESTORE_ID, rs.getString("sanctionfileStoreId"));
                additionalDetails.put(WCConstants.ESTIMATION_DATE_CONST, rs.getBigDecimal("estimationLetterDate"));
                additionalDetails.put(WCConstants.LOCALITY, rs.getString("locality"));
                currentWaterConnection.setAdditionalDetails(additionalDetails);
                currentWaterConnection
                        .processInstance(ProcessInstance.builder().action((rs.getString("action"))).build());
                currentWaterConnection.setPropertyId(rs.getString("property_id"));
                // Add documents id's
                currentWaterConnection.setConnectionExecutionDate(rs.getLong("connectionExecutionDate"));
                currentWaterConnection.setApplicationType(rs.getString("applicationType"));
                currentWaterConnection.setDateEffectiveFrom(rs.getLong("dateEffectiveFrom"));

                AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("ws_createdBy"))
                        .createdTime(rs.getLong("ws_createdTime")).lastModifiedBy(rs.getString("ws_lastModifiedBy"))
                        .lastModifiedTime(rs.getLong("ws_lastModifiedTime")).build();
                currentWaterConnection.setAuditDetails(auditdetails);

                connectionListMap.put(Id, currentWaterConnection);
            }
            addChildrenToProperty(rs, currentWaterConnection);
        }
        return new ArrayList<>(connectionListMap.values());
    }

    private void addChildrenToProperty(ResultSet rs, WaterConnection waterConnection) throws SQLException {
        addDocumentToWaterConnection(rs, waterConnection);
        addPlumberInfoToWaterConnection(rs, waterConnection);
        addHoldersDeatilsToWaterConnection(rs, waterConnection);
        addTaxHeadRaoadTypeDetailsToWaterConnection(rs, waterConnection);
    }

    private void addDocumentToWaterConnection(ResultSet rs, WaterConnection waterConnection) throws SQLException {
        String document_Id = rs.getString("doc_Id");
        String isActive = rs.getString("doc_active");
        boolean documentActive = false;
        if (!StringUtils.isEmpty(isActive)) {
            documentActive = Status.ACTIVE.name().equalsIgnoreCase(isActive);
        }
        if (!StringUtils.isEmpty(document_Id) && documentActive) {
            Document applicationDocument = new Document();
            applicationDocument.setId(document_Id);
            applicationDocument.setDocumentType(rs.getString("documenttype"));
            applicationDocument.setFileStoreId(rs.getString("filestoreid"));
            applicationDocument.setDocumentUid(rs.getString("doc_Id"));
            applicationDocument.setStatus(Status.fromValue(isActive));
            waterConnection.addDocumentsItem(applicationDocument);
        }
    }

    private void addPlumberInfoToWaterConnection(ResultSet rs, WaterConnection waterConnection) throws SQLException {
        String plumber_id = rs.getString("plumber_id");
        if (!StringUtils.isEmpty(plumber_id)) {
            PlumberInfo plumber = new PlumberInfo();
            plumber.setId(plumber_id);
            plumber.setName(rs.getString("plumber_name"));
            plumber.setGender(rs.getString("plumber_gender"));
            plumber.setLicenseNo(rs.getString("licenseno"));
            plumber.setMobileNumber(rs.getString("plumber_mobileNumber"));
            plumber.setRelationship(rs.getString("relationship"));
            plumber.setCorrespondenceAddress(rs.getString("correspondenceaddress"));
            plumber.setFatherOrHusbandName(rs.getString("fatherorhusbandname"));
            waterConnection.addPlumberInfoItem(plumber);
        }
    }

    private void addHoldersDeatilsToWaterConnection(ResultSet rs, WaterConnection waterConnection) throws SQLException {
        String uuid = rs.getString("userid");
        List<OwnerInfo> connectionHolders = waterConnection.getConnectionHolders();
        if (!CollectionUtils.isEmpty(connectionHolders)) {
            for (OwnerInfo connectionHolderInfo : connectionHolders) {
                if (!StringUtils.isEmpty(connectionHolderInfo.getUuid()) && !StringUtils.isEmpty(uuid) && connectionHolderInfo.getUuid().equals(uuid))
                    return;
            }
        }
        if(!StringUtils.isEmpty(uuid)){
            Double holderShipPercentage = rs.getDouble("holdershippercentage");
            if (rs.wasNull()) {
                holderShipPercentage = null;
            }
            Boolean isPrimaryOwner = rs.getBoolean("isprimaryholder");
            if (rs.wasNull()) {
                isPrimaryOwner = null;
            }
            OwnerInfo connectionHolderInfo = OwnerInfo.builder()
                    .relationship(Relationship.fromValue(rs.getString("holderrelationship")))
                    .status(Status.fromValue(rs.getString("holderstatus")))
                    .tenantId(rs.getString("holdertenantid")).ownerType(rs.getString("connectionholdertype"))
                    .isPrimaryOwner(isPrimaryOwner).uuid(uuid).build();
            waterConnection.addConnectionHolderInfo(connectionHolderInfo);
        }
    }
    
    private void addTaxHeadRaoadTypeDetailsToWaterConnection(ResultSet rs, WaterConnection waterConnection) throws SQLException {
   	 if(rs.getString("taxhead_id")!=null){
            WsTaxHeads wsTaxHeads = WsTaxHeads.builder()
                    .taxHeadCode(rs.getString("taxhead"))
                    .amount(rs.getBigDecimal("taxhead_amt"))
                    .id(rs.getString("taxhead_id"))
                    .active(rs.getBoolean("taxhead_active"))
                    .build();
            waterConnection.addWsTaxHead(wsTaxHeads);
        }
   	 
   	 if(rs.getString("roadtype_id")!=null){
            RoadTypeEst roadTypeEst = RoadTypeEst.builder()
                    .roadType(rs.getString("roadtype1"))
                    .length(rs.getBigDecimal("roadtype_length"))
                    .breadth(rs.getBigDecimal("roadtype_breadth"))
                    .depth(rs.getBigDecimal("roadtype_depth"))
                    .rate(rs.getBigDecimal("roadtype_rate"))
                    .id(rs.getString("roadtype_id"))
                    .active(rs.getBoolean("roadtype_active"))
                    .build();
            waterConnection.addRoadTypeEst(roadTypeEst);
        }
   }
}

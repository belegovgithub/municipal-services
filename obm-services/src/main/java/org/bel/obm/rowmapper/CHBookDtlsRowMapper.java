package org.bel.obm.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bel.obm.constants.OBMConstant;
import org.bel.obm.models.AuditDetails;
import org.bel.obm.models.BankDetails;
import org.bel.obm.models.CHBookDtls;
import org.bel.obm.models.Document;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class CHBookDtlsRowMapper implements ResultSetExtractor<List<CHBookDtls>> {

	@Override
	public List<CHBookDtls> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, CHBookDtls> chBookDtlsMap = new LinkedHashMap<>();
		while (rs.next()) {
			String id = rs.getString("dtl_id");
			CHBookDtls currentCHbookDtls = chBookDtlsMap.get(id);

			if (currentCHbookDtls == null) {

				Long applicationDate = (Long) rs.getObject("applicationdate");
				AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("dtl_createdBy"))
						.createdTime(rs.getLong("dtl_createdTime")).lastModifiedBy(rs.getString("dtl_lastModifiedBy"))
						.lastModifiedTime(rs.getLong("dtl_lastModifiedTime")).build();
				currentCHbookDtls = CHBookDtls.builder().auditDetails(auditdetails).id(id)
						.accountId(rs.getString("uuid")).hallId(rs.getString("hallId")).purpose(rs.getString("purpose"))
						.purposeDescription(rs.getString("purposedescription")).timeSlotId(rs.getString("timeslotid"))
						.residentTypeId(rs.getString("residenttypeid")).fromDate(rs.getLong("fromdate")).toDate(rs.getLong("todate"))
						.category(rs.getString("category")).applicationNumber(rs.getString("applicationnumber"))
						.applicationDate(applicationDate).tenantId(rs.getString("tenantid"))
						.action(rs.getString("action")).status(rs.getString("status"))
						.businessService(OBMConstant.businessService_CHB).workflowCode(OBMConstant.workflowCode_CHB).build();
				chBookDtlsMap.put(id, currentCHbookDtls);
			}
			addChildrenToProperty(rs, currentCHbookDtls);

		}
		return new ArrayList<>(chBookDtlsMap.values());
	}

	private void addChildrenToProperty(ResultSet rs, CHBookDtls currentCHbookDtls) throws SQLException {

		String tenantId = currentCHbookDtls.getTenantId();
		String bankId = rs.getString("bank_id");
		if (currentCHbookDtls.getBankDetails() == null) {
			AuditDetails auditDetails = AuditDetails.builder().createdBy(rs.getString("bank_createdBy"))
					.createdTime(rs.getLong("bank_createdTime")).lastModifiedBy(rs.getString("bank_lastModifiedBy"))
					.lastModifiedTime(rs.getLong("bank_lastModifiedTime")).build();
			BankDetails detail = BankDetails.builder().auditDetails(auditDetails).id(bankId)
					.accountHolderName(rs.getString("accountHolderName")).accountNumber(rs.getString("accountNumber"))
					.accountType(rs.getString("accountType")).nameOfBank(rs.getString("nameOfBank"))
					.ifscCode(rs.getString("ifscCode")).build();
			currentCHbookDtls.setBankDetails(detail);
		}
		if (rs.getString("chb_ap_doc_id") != null && rs.getBoolean("active")) {
			Document applicationDocument = Document.builder().documentType(rs.getString("documenttype"))
					.fileStoreId(rs.getString("filestoreid")).id(rs.getString("chb_ap_doc_id")).tenantId(tenantId)
					.active(rs.getBoolean("active")).build();
			currentCHbookDtls.addApplicationDocumentsItem(applicationDocument);
		}
	}
}

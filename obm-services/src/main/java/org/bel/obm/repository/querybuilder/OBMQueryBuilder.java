package org.bel.obm.repository.querybuilder;

import java.util.List;

import org.bel.obm.constants.OBMConfiguration;
import org.bel.obm.models.SearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OBMQueryBuilder {

	@Autowired
	private OBMConfiguration config;

	private final String paginationWrapper = "SELECT * FROM "
			+ "(SELECT *, DENSE_RANK() OVER (ORDER BY dtl_lastModifiedTime DESC , dtl_id) offset_ FROM " + "({})"
			+ " result) result_offset " + "WHERE offset_ > ? AND offset_ <= ?";

	private static final String CHBOOK_SEARCH_QUERY = "select dtl.*,bank.*,doc.* ,dtl.id as dtl_id,dtl.accountId as uuid, dtl.lastModifiedTime as dtl_lastModifiedTime,"
			+ "dtl.createdBy as dtl_createdBy,dtl.lastModifiedBy as dtl_lastModifiedBy,dtl.createdTime as dtl_createdTime,bank.id as bank_id,"
			+ "bank.lastModifiedTime as bank_lastModifiedTime,bank.createdBy as bank_createdBy,bank.lastModifiedBy as bank_lastModifiedBy,"
			+ "bank.createdTime as bank_createdTime, doc.id as chb_ap_doc_id from eg_obm_chb_dtls dtl "
			+ "left join eg_obm_chb_bank_dtls bank on  bank.chbdtlid = dtl.id left join eg_obm_chb_applicationdocument doc on doc.chbdtlid = dtl.id ";

	private final String CHBOOK_DELETE_APPDOC_QRY = "Delete from eg_obm_chb_applicationdocument where id IN (:ids)";

	public String getCHBookDtsSearchQuery(org.bel.obm.models.SearchCriteria criteria, List<Object> preparedStmtList) {
		StringBuilder builder = new StringBuilder(CHBOOK_SEARCH_QUERY);
		if (criteria.getAccountId() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" dtl.accountid = ? ");
			preparedStmtList.add(criteria.getAccountId());

			List<String> userIds = criteria.getUserIds();
			if (!CollectionUtils.isEmpty(userIds)) {
				builder.append(" OR (dtl.accountid IN (").append(createQuery(userIds)).append("))");
				addToPreparedStatement(preparedStmtList, userIds);
			}

		} else {
			if (criteria.getTenantId() != null) {
				addClauseIfRequired(preparedStmtList, builder);
				builder.append(" dtl.tenantid=? ");
				preparedStmtList.add(criteria.getTenantId());
			}
			List<String> tenantIds = criteria.getTenantIds();
			if (!CollectionUtils.isEmpty(tenantIds)) {
				addClauseIfRequired(preparedStmtList, builder);
				builder.append(" dtl.tenantid IN (").append(createQuery(tenantIds)).append(")");
				addToPreparedStatement(preparedStmtList, tenantIds);
			}
			List<String> ids = criteria.getIds();
			if (!CollectionUtils.isEmpty(ids)) {
				addClauseIfRequired(preparedStmtList, builder);
				builder.append(" dtl.id IN (").append(createQuery(ids)).append(")");
				addToPreparedStatement(preparedStmtList, ids);
			}
			List<String> userIds = criteria.getUserIds();
			if (!CollectionUtils.isEmpty(userIds)) {
				addClauseIfRequired(preparedStmtList, builder);
				builder.append("(dtl.accountid IN (").append(createQuery(userIds)).append("))");
				addToPreparedStatement(preparedStmtList, userIds);
			}
			if (criteria.getApplicationNumber() != null) {
				addClauseIfRequired(preparedStmtList, builder);
				builder.append(" dtl.applicationnumber = ? ");
				preparedStmtList.add(criteria.getApplicationNumber());
			}
			if (criteria.getStatus() != null) {
				addClauseIfRequired(preparedStmtList, builder);
				builder.append(" dtl.status = ? ");
				preparedStmtList.add(criteria.getStatus());
			}
			if (criteria.getFromDate() != null) {
				addClauseIfRequired(preparedStmtList, builder);
				builder.append(" dtl.applicationDate >= ? ");
				preparedStmtList.add(criteria.getFromDate());
			}
			if (criteria.getToDate() != null) {
				addClauseIfRequired(preparedStmtList, builder);
				builder.append(" dtl.applicationDate <= ? ");
				preparedStmtList.add(criteria.getToDate());
			}
		}
		return addPaginationWrapper(builder.toString(), preparedStmtList, criteria);
	}

	private void addToPreparedStatement(List<Object> preparedStmtList, List<String> ids) {
		ids.forEach(id -> {
			preparedStmtList.add(id);
		});
	}

	private String addPaginationWrapper(String query, List<Object> preparedStmtList, SearchCriteria criteria) {
		int limit = config.getDefaultOBMLimit();
		int offset = config.getDefaultOffset();
		String finalQuery = paginationWrapper.replace("{}", query);

		if (criteria.getLimit() != null && criteria.getLimit() <= config.getMaxSearchLimit())
			limit = criteria.getLimit();

		if (criteria.getLimit() != null && criteria.getLimit() > config.getMaxSearchLimit())
			limit = config.getMaxSearchLimit();

		if (criteria.getOffset() != null)
			offset = criteria.getOffset();

		preparedStmtList.add(offset);
		preparedStmtList.add(limit + offset);

		return finalQuery;
	}

	private static void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
		if (values.isEmpty())
			queryString.append(" WHERE ");
		else {
			queryString.append(" AND");
		}
	}

	private String createQuery(List<String> ids) {
		StringBuilder builder = new StringBuilder();
		int length = ids.size();
		for (int i = 0; i < length; i++) {
			builder.append(" ?");
			if (i != length - 1)
				builder.append(",");
		}
		return builder.toString();
	}

	public String deleteApplDocsCHB() {
		StringBuilder builder = new StringBuilder(CHBOOK_DELETE_APPDOC_QRY);
		return builder.toString();
	}
}

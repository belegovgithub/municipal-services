package org.egov.pt.repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import org.egov.pt.models.Assessment;
import org.egov.pt.models.AssessmentSearchCriteria;
import org.egov.pt.models.AuditDetails;
import org.egov.pt.models.Demand;
import org.egov.pt.models.DemandDetail;
import org.egov.pt.models.Property;
import org.egov.pt.models.PropertyCriteria;
import org.egov.pt.repository.builder.AssessmentQueryBuilder;
import org.egov.pt.repository.rowmapper.AssessmentRowMapper;
import org.egov.pt.web.contracts.DemandResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

@Repository
@Slf4j
public class AssessmentRepository {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	private AssessmentQueryBuilder queryBuilder;

	@Autowired
	private AssessmentRowMapper rowMapper;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public static final String DEMANDDTL_LEGACY_INSERT_QUERY = "INSERT INTO egbs_demanddetail_v1_legacy "
			+ "(id, demanddtlid, collectionamount, createdby, createdtime, lastmodifiedby, lastmodifiedtime) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?);";
	
	public List<Assessment> getAssessments(AssessmentSearchCriteria criteria){
		Map<String, Object> preparedStatementValues = new HashMap<>();
		List<Assessment> assessments = new ArrayList<>();
		String query = queryBuilder.getSearchQuery(criteria, preparedStatementValues);
		log.info("Query: "+query);
		log.debug("preparedStatementValues: "+preparedStatementValues);
		assessments = namedParameterJdbcTemplate.query(query, preparedStatementValues, rowMapper);
		return assessments;
	}

	public List<String> fetchAssessmentNumbers(AssessmentSearchCriteria criteria) {
		Map<String, Object> preparedStatementValues = new HashMap<>();
		String basequery = "SELECT assessmentnumber from eg_pt_asmt_assessment";
		StringBuilder builder = new StringBuilder(basequery);
		if (!ObjectUtils.isEmpty(criteria.getTenantId())) {
			builder.append(" where tenantid = :tenantid");
			preparedStatementValues.put("tenantid", criteria.getTenantId());
		}
		String orderbyClause = " ORDER BY createdtime,id offset :offset limit :limit";
		preparedStatementValues.put("offset", criteria.getOffset());
		preparedStatementValues.put("limit", criteria.getLimit());
		builder.append(orderbyClause);
		return namedParameterJdbcTemplate.query(builder.toString(),
				preparedStatementValues,
				new SingleColumnRowMapper<>(String.class));
	}

	public List<Assessment> getAssessmentPlainSearch(AssessmentSearchCriteria criteria) {
		if ((criteria.getAssessmentNumbers() == null || criteria.getAssessmentNumbers().isEmpty())
				&& (criteria.getIds() == null || criteria.getIds().isEmpty())
				&& (criteria.getPropertyIds() == null || criteria.getPropertyIds().isEmpty()))
			throw new CustomException("PLAIN_SEARCH_ERROR", "Empty search not allowed!");
		return getAssessments(criteria);
	}
	/**
	 * Fetches the assessment from DB corresponding to given assessment for update
	 * @param assessment THe Assessment to be updated
	 * @return Assessment from DB
	 */
	public Assessment getAssessmentFromDB(Assessment assessment){

		AssessmentSearchCriteria criteria = AssessmentSearchCriteria.builder()
				.ids(Collections.singleton(assessment.getId()))
				.tenantId(assessment.getTenantId())
				.build();

		List<Assessment> assessments = getAssessments(criteria);

		if(CollectionUtils.isEmpty(assessments))
			throw new CustomException("ASSESSMENT_NOT_FOUND","The assessment with id: "+assessment.getId()+" is not found in DB");

		return assessments.get(0);
	}
	
	public List<Assessment> getAssessmentsFromDBByPropertyId(Assessment assessment) {

		AssessmentSearchCriteria criteria = AssessmentSearchCriteria.builder()
				.propertyIds(Collections.singleton(assessment.getPropertyId())).tenantId(assessment.getTenantId())
				.build();

		List<Assessment> assessments = getAssessments(criteria);

		if (CollectionUtils.isEmpty(assessments))
			throw new CustomException("ASSESSMENT_NOT_FOUND",
					"No assessments are found in DB for the property: " + assessment.getPropertyId());

		return assessments;
	}

	public void saveDemandDtlForLegacy(DemandResponse response) {
		try {
			List<DemandDetail> demandDetails = new ArrayList<DemandDetail>();
			for(Demand demand : response.getDemands()) {
				for(DemandDetail demandDetail : demand.getDemandDetails()) {
					if(null!=demandDetail.getCollectionAmount() &&
							demandDetail.getCollectionAmount().compareTo(BigDecimal.ZERO) >0) {
						demandDetails.add(demandDetail);
					}
				}
			}
			log.info("demandDetails.size() : "+demandDetails.size());
			if(demandDetails.size()>0) {
				jdbcTemplate.batchUpdate(DEMANDDTL_LEGACY_INSERT_QUERY, new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int rowNum) throws SQLException {
						
						DemandDetail demandDetail = demandDetails.get(rowNum);
						AuditDetails auditDetail = demandDetail.getAuditDetails();
						ps.setString(1, UUID.randomUUID().toString());
						ps.setString(2, demandDetail.getId());
						ps.setBigDecimal(3, demandDetail.getCollectionAmount());
						ps.setString(4, auditDetail.getCreatedBy());
						ps.setLong(5, auditDetail.getCreatedTime());
						ps.setString(6, auditDetail.getLastModifiedBy());
						ps.setLong(7, auditDetail.getLastModifiedTime());
					}

					@Override
					public int getBatchSize() {
						return demandDetails.size();
					}
				});
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}


}

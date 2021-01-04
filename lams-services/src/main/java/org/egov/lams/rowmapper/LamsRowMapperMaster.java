package org.egov.lams.rowmapper;


import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.egov.lams.web.models.LeaseAgreementRenewal;
import org.egov.lams.web.models.LeaseAgreementRenewalDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;



@Component
public class LamsRowMapperMaster  implements ResultSetExtractor<List<LeaseAgreementRenewalDetail>> {
	@Autowired
    private ObjectMapper mapper;
	
	@Override
	public List<LeaseAgreementRenewalDetail> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, LeaseAgreementRenewalDetail> leaseAgreementMap = new LinkedHashMap<>();
		while (rs.next()) {
            String id = rs.getString("survey_id");
            LeaseAgreementRenewalDetail currentRenewal = leaseAgreementMap.get(id);

            if(currentRenewal == null){
            	DecimalFormat df = new DecimalFormat("#.###");
                currentRenewal = LeaseAgreementRenewalDetail.builder()
    					.area(Double.parseDouble(df.format(((BigDecimal) rs.getObject("area")).doubleValue()))+"  "+rs.getString("unitname"))
    					.surveyId(id)
    					.lesseAsPerGLR(rs.getString("lessee"))
    					.surveyNo(rs.getString("surveyno"))
//    					.finalTermExpiryDate((Long) rs.getObject("finaltermexpirydate"))
    					.located(rs.getString("location"))
    					.mutationId(rs.getString("mutationid"))
    					.detailsAndMutDate(rs.getString("mutation_desc"))
    					//.areaUnit(rs.getString("unitname"))
    					.description(rs.getString("description"))
    					.classSurvey(rs.getString("classsurvey"))
    					.managedBy(rs.getString("by_whom_manage"))
    					.landLord(rs.getString("landlord"))
    					.holderOfOccupancyRights(rs.getString("holdersrights"))
    					.remarks(rs.getString("remarks"))
    					.volume(rs.getString("volumeno"))
    					.pageOfRegister(rs.getString("pageno"))
    					.natureOfHolderRights(rs.getString("natureofholdersrights"))
    					.rentTowardsCentGovt(rs.getString("rent_central_govt"))
    					.rentTowardsCB(rs.getString("rent_cantt_board"))
    					.build();
                leaseAgreementMap.put(id,currentRenewal);
            }

        }
        return new ArrayList<>(leaseAgreementMap.values());
	}

	}

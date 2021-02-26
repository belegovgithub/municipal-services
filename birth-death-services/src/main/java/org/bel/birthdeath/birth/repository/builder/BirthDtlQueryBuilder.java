package org.bel.birthdeath.birth.repository.builder;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.bel.birthdeath.birth.model.SearchCriteria;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BirthDtlQueryBuilder {


    private static final String QUERY_Master = "SELECT bdtl.id birthdtlid, tenantid, registrationno, dateofbirth, counter, gender, "+
    		"bfat.firstname bfatfn ,bmot.firstname bmotfn , bdtl.firstname bdtlfn "+
    		"FROM public.eg_birth_dtls bdtl " + 
    		"left join eg_birth_father_info bfat on bfat.birthdtlid = bdtl.id " + 
    		"left join eg_birth_mother_info bmot on bmot.birthdtlid = bdtl.id " ;

    private static void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
        if (values.isEmpty())
            queryString.append(" WHERE ");
        else {
            queryString.append(" AND");
        }
    }


	public String getBirtDtls(SearchCriteria criteria, List<Object> preparedStmtList) {
		StringBuilder builder = new StringBuilder(QUERY_Master);

		if (criteria.getTenantId() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" bdtl.tenantid=? ");
			preparedStmtList.add(criteria.getTenantId());
		}
		if (criteria.getRegistrationNo() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" bdtl.registrationno=? ");
			preparedStmtList.add(criteria.getRegistrationNo());
		}
		if (criteria.getGender() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" bdtl.gender=? ");
			preparedStmtList.add(criteria.getGender());
		}
		if (criteria.getHospitalId() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" bdtl.hospitalid=? ");
			preparedStmtList.add(criteria.getHospitalId());
		}
		if (criteria.getMotherName() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" bmot.firstname ilike ?");
			preparedStmtList.add("%"+criteria.getMotherName()+"%");
		}
		if (criteria.getId() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" bdtl.id=? ");
			preparedStmtList.add(criteria.getId());
		}
		if (criteria.getDateOfBirth() != null) {
			SimpleDateFormat sdf= new SimpleDateFormat("dd-MM-yyyy");
			try {
				Date dob = sdf.parse(criteria.getDateOfBirth());
				//Timestamp ts = new Timestamp(dob.getTime());
				addClauseIfRequired(preparedStmtList, builder);
				builder.append(" CAST(bdtl.dateofbirth as DATE)=?");
				preparedStmtList.add(dob);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return builder.toString();
	}

}

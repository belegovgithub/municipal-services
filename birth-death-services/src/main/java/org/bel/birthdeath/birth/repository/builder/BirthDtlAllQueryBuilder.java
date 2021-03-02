package org.bel.birthdeath.birth.repository.builder;

import java.util.List;

import org.bel.birthdeath.birth.model.SearchCriteria;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BirthDtlAllQueryBuilder {

	
    private static String QUERY_Master = "SELECT bdtl.id birthdtlid, bdtl.tenantid tenantid, registrationno, dateofbirth, now() as dateofissue , counter,  "
    		+ "CASE WHEN gender = '1' THEN 'Male' WHEN gender = '2' THEN 'Female' WHEN gender = '3' THEN 'Transgender'  END AS genderstr ,"
    		+ "CASE WHEN hospitalid = '0' THEN hospitalname ELSE (select bh.hospitalname from eg_birth_death_hospitals bh where bh.id=hospitalid) END AS hospitalname ,"
    		+ "bfat.firstname bfatfn ,bmot.firstname bmotfn , bdtl.firstname bdtlfn ,placeofbirth ,dateofreport, remarks, "
    		+ "bfat.middlename bfatmn ,bmot.middlename bmotmn , bdtl.middlename bdtlmn ,"
    		+ "bfat.lastname bfatln ,bmot.lastname bmotln , bdtl.lastname bdtlln ,"
    		+ "concat(bpmad.houseno,' ',bpmad.buildingno,' ',bpmad.streetname,' ',bpmad.locality,' ',bpmad.tehsil,' ',"
    		+ "bpmad.district,' ',bpmad.city,'',bpmad.state,' ',bpmad.pinno,' ',bpmad.country ) as permaddress ,"
    		+ "concat(bpsad.houseno,' ',bpsad.buildingno,' ',bpsad.streetname,' ',bpsad.locality,' ',bpsad.tehsil,' ',"
    		+ "bpsad.district,' ',bpsad.city,' ',bpsad.state,' ',bpsad.pinno,' ',bpsad.country ) as presentaddress "+
    		"FROM public.eg_birth_dtls bdtl " + 
    		"left join eg_birth_father_info bfat on bfat.birthdtlid = bdtl.id " + 
    		"left join eg_birth_mother_info bmot on bmot.birthdtlid = bdtl.id " +
    		"left join eg_birth_permaddr bpmad on bpmad.birthdtlid = bdtl.id " + 
    		"left join eg_birth_presentaddr bpsad on bpsad.birthdtlid = bdtl.id ";
    
    private static void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
        if (values.isEmpty())
            queryString.append(" WHERE ");
        else {
            queryString.append(" AND");
        }
    }


	public String getBirthCertReq(String consumerCode, List<Object> preparedStmtList) {
		StringBuilder builder = new StringBuilder("select * from eg_birth_cert_request ");
		if (consumerCode != null && !consumerCode.isEmpty()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" birthcertificateno=? ");
			preparedStmtList.add(consumerCode);
		}
		return builder.toString();
	}

	public String getBirtDtlsAll(SearchCriteria criteria, List<Object> preparedStmtList) {
		StringBuilder builder = new StringBuilder(QUERY_Master);

		if (criteria.getTenantId() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" bdtl.tenantid=? ");
			preparedStmtList.add(criteria.getTenantId());
		}
		if (criteria.getId() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" bdtl.id=? ");
			preparedStmtList.add(criteria.getId());
		}
		return builder.toString();
	}
}

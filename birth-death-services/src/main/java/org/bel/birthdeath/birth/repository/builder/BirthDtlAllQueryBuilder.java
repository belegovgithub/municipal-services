package org.bel.birthdeath.birth.repository.builder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.bel.birthdeath.birth.model.SearchCriteria;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BirthDtlAllQueryBuilder {

	
    private static String QUERY_Master = "SELECT bdtl.id birthdtlid, bdtl.tenantid tenantid, registrationno, dateofbirth, counter, gender, "
    		+ "bfat.firstname bfatfn ,bmot.firstname bmotfn , bdtl.firstname bdtlfn ,placeofbirth ,dateofreport, remarks, "
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

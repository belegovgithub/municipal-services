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

    private static String QUERY_Master_All = "SELECT bdtl.id birthdtlid, bdtl.tenantid tenantid, registrationno, dateofbirth, CURRENT_TIMESTAMP  as dateofissue , counter, gender , "
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
    
    private static final String QUERY_Master = "SELECT bdtl.id birthdtlid, tenantid, registrationno, dateofbirth, counter, gender , "+
    		"CASE WHEN gender = '1' THEN 'Male' WHEN gender = '2' THEN 'Female' WHEN gender = '3' THEN 'Transgender'  END AS genderstr ," + 
    		"CASE WHEN hospitalid = '0' THEN hospitalname ELSE (select bh.hospitalname from eg_birth_death_hospitals bh where bh.id=hospitalid) END AS hospitalname ,"+
    		"bfat.firstname bfatfn ,bmot.firstname bmotfn , bdtl.firstname bdtlfn ,"+
    		"bfat.middlename bfatmn ,bmot.middlename bmotmn , bdtl.middlename bdtlmn ,"+
    		"bfat.lastname bfatln ,bmot.lastname bmotln , bdtl.lastname bdtlln "+
    		"FROM public.eg_birth_dtls bdtl " + 
    		"left join eg_birth_father_info bfat on bfat.birthdtlid = bdtl.id " + 
    		"left join eg_birth_mother_info bmot on bmot.birthdtlid = bdtl.id " ;
    
    private static String applsQuery ="select breq.birthCertificateNo, breq.createdtime, breq.status, bdtl.registrationno, bdtl.tenantid, "
    		+ "concat(COALESCE(bdtl.firstname,'') , ' ', COALESCE(bdtl.middlename,'') ,' ', COALESCE(bdtl.lastname,'')) as name "
    		+ "from eg_birth_cert_request breq left join eg_birth_dtls bdtl on bdtl.id=breq.birthDtlId where  "
    		+ "breq.createdby=? order by breq.createdtime DESC ";
    
    private static void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
        if (values.isEmpty())
            queryString.append(" WHERE ");
        else {
            queryString.append(" AND");
        }
    }


	public String getBirthCertReq(String consumerCode, List<Object> preparedStmtList) {
		StringBuilder builder = new StringBuilder("select req.*,(select tenantid from eg_birth_dtls dtl where req.birthdtlid=dtl.id) from eg_birth_cert_request req");
		if (consumerCode != null && !consumerCode.isEmpty()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" birthcertificateno=? ");
			preparedStmtList.add(consumerCode);
		}
		return builder.toString();
	}

	public String getBirtDtlsAll(SearchCriteria criteria, List<Object> preparedStmtList) {
		StringBuilder builder = new StringBuilder(QUERY_Master_All);

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


	public String searchApplications( String uuid, List<Object> preparedStmtList) {
		StringBuilder builder = new StringBuilder(applsQuery);
		preparedStmtList.add(uuid);
		return builder.toString();
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
			if(criteria.getHospitalId().equalsIgnoreCase("0")) {
				addClauseIfRequired(preparedStmtList, builder);
				builder.append(" bdtl.hospitalid is null ");
			}
			else {
				addClauseIfRequired(preparedStmtList, builder);
				builder.append(" bdtl.hospitalid=? ");
				preparedStmtList.add(criteria.getHospitalId());
			}
		}
		if (criteria.getMotherName() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" ( bmot.firstname ilike ? or bmot.middlename ilike ? or bmot.lastname ilike ? ) ");
			preparedStmtList.add("%"+criteria.getMotherName()+"%");
			preparedStmtList.add("%"+criteria.getMotherName()+"%");
			preparedStmtList.add("%"+criteria.getMotherName()+"%");
		}
		if (criteria.getFatherName() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" ( bfat.firstname ilike ? or bfat.middlename ilike ? or bfat.lastname ilike ? ) ");
			preparedStmtList.add("%"+criteria.getFatherName()+"%");
			preparedStmtList.add("%"+criteria.getFatherName()+"%");
			preparedStmtList.add("%"+criteria.getFatherName()+"%");
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

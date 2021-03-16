package org.bel.birthdeath.death.repository.builder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.bel.birthdeath.death.model.SearchCriteria;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DeathDtlAllQueryBuilder {

	
    private static String QUERY_Master_All = "SELECT bdtl.id deathdtlid, bdtl.tenantid tenantid, registrationno, dateofdeath, counter, gender , age , "
    		+ "CASE WHEN gender = '1' THEN 'Male' WHEN gender = '2' THEN 'Female' WHEN gender = '3' THEN 'Transgender'  END AS genderstr ,"
    		+ " (select bh.hospitalname from eg_birth_death_hospitals bh where bh.id=hospitalid)  AS hospitalname, placeofdeath, dateofreport, remarks, "
    		+ "bfat.firstname bfatfn ,bmot.firstname bmotfn , bdtl.firstname bdtlfn ,bsps.firstname bspsfn , "
    		+ "bfat.middlename bfatmn ,bmot.middlename bmotmn , bdtl.middlename bdtlmn ,bsps.middlename bspsmn , "
    		+ "bfat.lastname bfatln ,bmot.lastname bmotln , bdtl.lastname bdtlln ,bsps.lastname bspsln , "
    		+ "bpmad.houseno pmhouseno,bpmad.buildingno pmbuildingno,bpmad.streetname pmstreetname,bpmad.locality pmlocality,bpmad.tehsil pmtehsil,"
    		+ "bpmad.district pmdistrict,bpmad.city pmcity ,bpmad.state pmstate,bpmad.pinno pmpinno,bpmad.country pmcountry,"
    		+ "bpsad.houseno pshouseno,bpsad.buildingno psbuildingno,bpsad.streetname psstreetname,bpsad.locality pslocality,bpsad.tehsil pstehsil,"
    		+ "bpsad.district psdistrict,bpsad.city pscity ,bpsad.state psstate,bpsad.pinno pspinno,bpsad.country pscountry "+
    		"FROM public.eg_death_dtls bdtl " + 
    		"left join eg_death_father_info bfat on bfat.deathdtlid = bdtl.id " + 
    		"left join eg_death_mother_info bmot on bmot.deathdtlid = bdtl.id " +
    		"left join eg_death_permaddr bpmad on bpmad.deathdtlid = bdtl.id " + 
    		"left join eg_death_presentaddr bpsad on bpsad.deathdtlid = bdtl.id "+
    		"left join eg_death_spouse_info bsps on bsps.deathdtlid = bdtl.id " ;
    
    private static final String QUERY_Master = "SELECT bdtl.id deathdtlid, tenantid, registrationno, dateofdeath, counter, gender , "+
    		"CASE WHEN gender = '1' THEN 'Male' WHEN gender = '2' THEN 'Female' WHEN gender = '3' THEN 'Transgender'  END AS genderstr ," + 
    		"(select bh.hospitalname from eg_birth_death_hospitals bh where bh.id=hospitalid)  AS hospitalname ,"+
    		"bfat.firstname bfatfn ,bmot.firstname bmotfn , bdtl.firstname bdtlfn ,bsps.firstname bspsfn , "+
    		"bfat.middlename bfatmn ,bmot.middlename bmotmn , bdtl.middlename bdtlmn ,bsps.middlename bspsmn , "+
    		"bfat.lastname bfatln ,bmot.lastname bmotln , bdtl.lastname bdtlln ,bsps.lastname bspsln "+
    		"FROM public.eg_death_dtls bdtl " + 
    		"left join eg_death_father_info bfat on bfat.deathdtlid = bdtl.id " + 
    		"left join eg_death_mother_info bmot on bmot.deathdtlid = bdtl.id " +
    		"left join eg_death_spouse_info bsps on bsps.deathdtlid = bdtl.id " ;
    
    private static String applsQuery ="select breq.deathCertificateNo, breq.createdtime, breq.status, bdtl.registrationno, bdtl.tenantid, "
    		+ "concat(COALESCE(bdtl.firstname,'') , ' ', COALESCE(bdtl.middlename,'') ,' ', COALESCE(bdtl.lastname,'')) as name "
    		+ "from eg_death_cert_request breq left join eg_death_dtls bdtl on bdtl.id=breq.deathDtlId where  "
    		+ "breq.createdby=?";
    
    private static void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
        if (values.isEmpty())
            queryString.append(" WHERE ");
        else {
            queryString.append(" AND");
        }
    }


	public String getDeathCertReq(String consumerCode, List<Object> preparedStmtList) {
		StringBuilder builder = new StringBuilder("select req.*,(select tenantid from eg_death_dtls dtl where req.deathdtlid=dtl.id) from eg_death_cert_request req");
		if (consumerCode != null && !consumerCode.isEmpty()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" deathcertificateno=? ");
			preparedStmtList.add(consumerCode);
		}
		return builder.toString();
	}

	public String getDeathDtlsAll(SearchCriteria criteria, List<Object> preparedStmtList) {
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


	public String searchApplications(String tenantId, List<Object> preparedStmtList) {
		StringBuilder builder = new StringBuilder(applsQuery);
		preparedStmtList.add(tenantId);
		return builder.toString();
	}
	
	public String getDeathDtls(SearchCriteria criteria, List<Object> preparedStmtList) {
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
		if (criteria.getSpouseName() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" ( bfat.firstname ilike ? or bfat.middlename ilike ? or bfat.lastname ilike ? ) ");
			preparedStmtList.add("%"+criteria.getSpouseName()+"%");
			preparedStmtList.add("%"+criteria.getSpouseName()+"%");
			preparedStmtList.add("%"+criteria.getSpouseName()+"%");
		}
		if (criteria.getId() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" bdtl.id=? ");
			preparedStmtList.add(criteria.getId());
		}
		if (criteria.getDateOfDeath() != null) {
			SimpleDateFormat sdf= new SimpleDateFormat("dd-MM-yyyy");
			try {
				Date dob = sdf.parse(criteria.getDateOfDeath());
				//Timestamp ts = new Timestamp(dob.getTime());
				addClauseIfRequired(preparedStmtList, builder);
				builder.append(" CAST(bdtl.dateofdeath as DATE)=?");
				preparedStmtList.add(dob);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		if(criteria.getName() !=null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" ( bdtl.firstname ilike ? or bdtl.middlename ilike ? or bdtl.lastname ilike ? )");
			preparedStmtList.add("%"+criteria.getName()+"%");
			preparedStmtList.add("%"+criteria.getName()+"%");
			preparedStmtList.add("%"+criteria.getName()+"%");
		}
		return builder.toString();
	}

}

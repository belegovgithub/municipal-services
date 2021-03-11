package org.bel.birthdeath.common.repository;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bel.birthdeath.birth.model.EgBirthDtl;
import org.bel.birthdeath.birth.model.EgBirthFatherInfo;
import org.bel.birthdeath.birth.model.EgBirthMotherInfo;
import org.bel.birthdeath.birth.model.EgBirthPermaddr;
import org.bel.birthdeath.birth.model.EgBirthPresentaddr;
import org.bel.birthdeath.birth.validator.BirthValidator;
import org.bel.birthdeath.common.contract.BirthResponse;
import org.bel.birthdeath.common.model.AuditDetails;
import org.bel.birthdeath.common.model.EgHospitalDtl;
import org.bel.birthdeath.common.repository.builder.CommonQueryBuilder;
import org.bel.birthdeath.common.repository.rowmapper.CommonRowMapper;
import org.bel.birthdeath.utils.CommonUtils;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class CommonRepository {
	
	@Autowired
	private ObjectMapper mapper;

	@Autowired
    private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private CommonQueryBuilder queryBuilder;
	
	@Autowired
	private CommonRowMapper rowMapper;
	
	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
	@Autowired
	private CommonUtils commUtils;
	
	@Autowired
	BirthValidator birthValidator;
    
	private static final String birthDtlSaveQry="INSERT INTO public.eg_birth_dtls(id, registrationno, hospitalname, dateofreport, "
    		+ "dateofbirth, firstname, middlename, lastname, placeofbirth, informantsname, informantsaddress, "
    		+ "createdtime, createdby, lastmodifiedtime, lastmodifiedby, counter, tenantid, gender, remarks, hospitalid) "
    		+ "VALUES (:id, :registrationno, :hospitalname, :dateofreport, :dateofbirth, :firstname, :middlename, :lastname, "
    		+ ":placeofbirth, :informantsname, :informantsaddress, :createdtime, :createdby, :lastmodifiedtime, "
    		+ ":lastmodifiedby, :counter, :tenantid, :gender, :remarks, :hospitalid); ";
	
	private static final String birthFatherInfoSaveQry="INSERT INTO public.eg_birth_father_info( id, firstname, middlename, lastname, aadharno, "
			+ "emailid, mobileno, education, proffession, nationality, religion, createdtime, createdby, lastmodifiedtime, lastmodifiedby, birthdtlid) "
			+ "VALUES (:id, :firstname, :middlename, :lastname, :aadharno, :emailid, :mobileno, :education, :proffession, :nationality,"
			+ " :religion, :createdtime, :createdby, :lastmodifiedtime, :lastmodifiedby, :birthdtlid);";
	
	private static final String birthMotherInfoSaveQry="INSERT INTO public.eg_birth_mother_info(id, firstname, middlename, lastname, aadharno, "
			+ "emailid, mobileno, education, proffession, nationality, religion, createdtime, createdby, lastmodifiedtime, lastmodifiedby, birthdtlid) "
			+ "VALUES (:id, :firstname, :middlename, :lastname, :aadharno, :emailid, :mobileno, :education, :proffession, :nationality,"
			+ " :religion, :createdtime, :createdby, :lastmodifiedtime, :lastmodifiedby, :birthdtlid);";
	
	private static final String birthPermAddrSaveQry="INSERT INTO public.eg_birth_permaddr(id, buildingno, houseno, streetname, locality, tehsil, "
			+ "district, city, state, pinno, country, createdby, createdtime, lastmodifiedby, lastmodifiedtime, birthdtlid) "
			+ "VALUES (:id, :buildingno, :houseno, :streetname, :locality, :tehsil, :district, :city, :state, :pinno, :country,"
			+ " :createdby, :createdtime, :lastmodifiedby, :lastmodifiedtime, :birthdtlid);";
	
	private static final String birthPresentAddrSaveQry="INSERT INTO public.eg_birth_presentaddr(id, buildingno, houseno, streetname, locality, tehsil, "
			+ "district, city, state, pinno, country, createdby, createdtime, lastmodifiedby, lastmodifiedtime, birthdtlid) "
			+ "VALUES (:id, :buildingno, :houseno, :streetname, :locality, :tehsil, :district, :city, :state, :pinno, :country, "
			+ ":createdby, :createdtime, :lastmodifiedby, :lastmodifiedtime, :birthdtlid);";
	
	public List<EgHospitalDtl> getHospitalDtls(String tenantId) {
		List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getHospitalDtls(tenantId, preparedStmtList);
        List<EgHospitalDtl> hospitalDtls =  jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
        return hospitalDtls;
	}

	public ArrayList<EgBirthDtl> saveBirthImport(BirthResponse response, RequestInfo requestInfo) {
		ArrayList<EgBirthDtl> birthArrayList = new ArrayList<EgBirthDtl>();
		try {
		//BirthResponse response= mapper.convertValue(importJSon, BirthResponse.class);
		List<MapSqlParameterSource> birthDtlSource = new ArrayList<>();
		List<MapSqlParameterSource> birthFatherInfoSource = new ArrayList<>();
		List<MapSqlParameterSource> birthMotherInfoSource = new ArrayList<>();
		List<MapSqlParameterSource> birthPermAddrSource = new ArrayList<>();
		List<MapSqlParameterSource> birthPresentAddrSource = new ArrayList<>();
		AuditDetails auditDetails = commUtils.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		for(EgBirthDtl birthDtl : response.getBirthCerts()) {
			if(birthValidator.validateUniqueRegNo(birthDtl)){
				birthDtlSource.add(getParametersForBirthDtl(birthDtl, auditDetails));
				birthFatherInfoSource.add(getParametersForFatherInfo(birthDtl, auditDetails));
				birthMotherInfoSource.add(getParametersForMotherInfo(birthDtl, auditDetails));
				birthPermAddrSource.add(getParametersForPermAddr(birthDtl, auditDetails));
				birthPresentAddrSource.add(getParametersForPresentAddr(birthDtl, auditDetails));
				log.info("bdtlid "+birthDtl.getId());
				birthArrayList.add(birthDtl);
			}
		}
		log.info(new Gson().toJson(birthDtlSource));
		namedParameterJdbcTemplate.batchUpdate(birthDtlSaveQry, birthDtlSource.toArray(new MapSqlParameterSource[0]));
		namedParameterJdbcTemplate.batchUpdate(birthFatherInfoSaveQry, birthFatherInfoSource.toArray(new MapSqlParameterSource[0]));
		namedParameterJdbcTemplate.batchUpdate(birthMotherInfoSaveQry, birthMotherInfoSource.toArray(new MapSqlParameterSource[0]));
		namedParameterJdbcTemplate.batchUpdate(birthPermAddrSaveQry, birthPermAddrSource.toArray(new MapSqlParameterSource[0]));
		namedParameterJdbcTemplate.batchUpdate(birthPresentAddrSaveQry, birthPresentAddrSource.toArray(new MapSqlParameterSource[0]));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return birthArrayList;
	}

	private MapSqlParameterSource getParametersForPresentAddr(EgBirthDtl birthDtl, AuditDetails auditDetails) {
		MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
		EgBirthPresentaddr presentaddr = birthDtl.getBirthPresentaddr();
		sqlParameterSource.addValue("id", UUID.randomUUID().toString());
		sqlParameterSource.addValue("buildingno", presentaddr.getBuildingno());
		sqlParameterSource.addValue("houseno", presentaddr.getHouseno());
		sqlParameterSource.addValue("streetname", presentaddr.getStreetname());
		sqlParameterSource.addValue("locality", presentaddr.getLocality());
		sqlParameterSource.addValue("tehsil", presentaddr.getTehsil());
		sqlParameterSource.addValue("district", presentaddr.getDistrict());
		sqlParameterSource.addValue("city", presentaddr.getCity());
		sqlParameterSource.addValue("state", presentaddr.getState());
		sqlParameterSource.addValue("pinno", presentaddr.getPinno());
		sqlParameterSource.addValue("country", presentaddr.getCountry());
		sqlParameterSource.addValue("createdby", auditDetails.getCreatedBy());
		sqlParameterSource.addValue("createdtime", auditDetails.getCreatedTime());
		sqlParameterSource.addValue("lastmodifiedby", null);
		sqlParameterSource.addValue("lastmodifiedtime", null);
		sqlParameterSource.addValue("birthdtlid", birthDtl.getId());
		return sqlParameterSource;
	}

	private MapSqlParameterSource getParametersForPermAddr(EgBirthDtl birthDtl, AuditDetails auditDetails) {
		MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
		EgBirthPermaddr permaddr = birthDtl.getBirthPermaddr();
		sqlParameterSource.addValue("id", UUID.randomUUID().toString());
		sqlParameterSource.addValue("buildingno", permaddr.getBuildingno());
		sqlParameterSource.addValue("houseno", permaddr.getHouseno());
		sqlParameterSource.addValue("streetname", permaddr.getStreetname());
		sqlParameterSource.addValue("locality", permaddr.getLocality());
		sqlParameterSource.addValue("tehsil", permaddr.getTehsil());
		sqlParameterSource.addValue("district", permaddr.getDistrict());
		sqlParameterSource.addValue("city", permaddr.getCity());
		sqlParameterSource.addValue("state", permaddr.getState());
		sqlParameterSource.addValue("pinno", permaddr.getPinno());
		sqlParameterSource.addValue("country", permaddr.getCountry());
		sqlParameterSource.addValue("createdby", auditDetails.getCreatedBy());
		sqlParameterSource.addValue("createdtime", auditDetails.getCreatedTime());
		sqlParameterSource.addValue("lastmodifiedby", null);
		sqlParameterSource.addValue("lastmodifiedtime", null);
		sqlParameterSource.addValue("birthdtlid", birthDtl.getId());
		return sqlParameterSource;
	}

	private MapSqlParameterSource getParametersForMotherInfo(EgBirthDtl birthDtl, AuditDetails auditDetails) {
		EgBirthMotherInfo birthMotherInfo = birthDtl.getBirthMotherInfo();
		MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
		sqlParameterSource.addValue("id", UUID.randomUUID().toString());
		sqlParameterSource.addValue("firstname", birthMotherInfo.getFirstname());
		sqlParameterSource.addValue("middlename", birthMotherInfo.getMiddlename());
		sqlParameterSource.addValue("lastname", birthMotherInfo.getLastname());
		sqlParameterSource.addValue("aadharno", birthMotherInfo.getAadharno());
		sqlParameterSource.addValue("emailid", birthMotherInfo.getEmailid());
		sqlParameterSource.addValue("mobileno", birthMotherInfo.getMobileno());
		sqlParameterSource.addValue("education", birthMotherInfo.getEducation());
		sqlParameterSource.addValue("proffession", birthMotherInfo.getProffession());
		sqlParameterSource.addValue("nationality", birthMotherInfo.getNationality());
		sqlParameterSource.addValue("religion", birthMotherInfo.getReligion());
		sqlParameterSource.addValue("createdtime", auditDetails.getCreatedTime());
		sqlParameterSource.addValue("createdby", auditDetails.getCreatedBy());
		sqlParameterSource.addValue("lastmodifiedtime", null);
		sqlParameterSource.addValue("lastmodifiedby", null);
		sqlParameterSource.addValue("birthdtlid", birthDtl.getId());
		return sqlParameterSource;
	}

	private MapSqlParameterSource getParametersForFatherInfo(EgBirthDtl birthDtl,
			AuditDetails auditDetails) {
		EgBirthFatherInfo birthFatherInfo = birthDtl.getBirthFatherInfo();
		MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
		sqlParameterSource.addValue("id", UUID.randomUUID().toString());
		sqlParameterSource.addValue("firstname", birthFatherInfo.getFirstname());
		sqlParameterSource.addValue("middlename", birthFatherInfo.getMiddlename());
		sqlParameterSource.addValue("lastname", birthFatherInfo.getLastname());
		sqlParameterSource.addValue("aadharno", birthFatherInfo.getAadharno());
		sqlParameterSource.addValue("emailid", birthFatherInfo.getEmailid());
		sqlParameterSource.addValue("mobileno", birthFatherInfo.getMobileno());
		sqlParameterSource.addValue("education", birthFatherInfo.getEducation());
		sqlParameterSource.addValue("proffession", birthFatherInfo.getProffession());
		sqlParameterSource.addValue("nationality", birthFatherInfo.getNationality());
		sqlParameterSource.addValue("religion", birthFatherInfo.getReligion());
		sqlParameterSource.addValue("createdtime", auditDetails.getCreatedTime());
		sqlParameterSource.addValue("createdby", auditDetails.getCreatedBy());
		sqlParameterSource.addValue("lastmodifiedtime", null);
		sqlParameterSource.addValue("lastmodifiedby", null);
		sqlParameterSource.addValue("birthdtlid", birthDtl.getId());
		return sqlParameterSource;
	}

	private MapSqlParameterSource getParametersForBirthDtl(EgBirthDtl birthDtl, AuditDetails auditDetails) {
		MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
		String id= UUID.randomUUID().toString();
		sqlParameterSource.addValue("id", id);
		sqlParameterSource.addValue("registrationno", birthDtl.getRegistrationno());
		sqlParameterSource.addValue("hospitalname", birthDtl.getHospitalname());
		sqlParameterSource.addValue("dateofreport", birthDtl.getDateofreport());
		sqlParameterSource.addValue("dateofbirth", birthDtl.getDateofbirth());
		sqlParameterSource.addValue("firstname", birthDtl.getFirstname());
		sqlParameterSource.addValue("middlename", birthDtl.getMiddlename());
		sqlParameterSource.addValue("lastname", birthDtl.getLastname());
		sqlParameterSource.addValue("placeofbirth", birthDtl.getPlaceofbirth());
		sqlParameterSource.addValue("informantsname", birthDtl.getInformantsname());
		sqlParameterSource.addValue("informantsaddress", birthDtl.getInformantsaddress());
		sqlParameterSource.addValue("createdtime", auditDetails.getCreatedTime());
		sqlParameterSource.addValue("createdby", auditDetails.getCreatedBy());
		sqlParameterSource.addValue("lastmodifiedtime", null);
		sqlParameterSource.addValue("lastmodifiedby", null);
		sqlParameterSource.addValue("counter", birthDtl.getCounter());
		sqlParameterSource.addValue("tenantid", birthDtl.getTenantid());
		sqlParameterSource.addValue("gender", birthDtl.getGender());
		sqlParameterSource.addValue("remarks", birthDtl.getRemarks());
		sqlParameterSource.addValue("hospitalid", birthDtl.getHospitalid());
		birthDtl.setId(id);
		return sqlParameterSource;

	}
}

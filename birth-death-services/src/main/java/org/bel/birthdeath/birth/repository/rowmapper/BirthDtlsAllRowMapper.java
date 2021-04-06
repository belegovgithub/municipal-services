package org.bel.birthdeath.birth.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bel.birthdeath.birth.model.EgBirthDtl;
import org.bel.birthdeath.birth.model.EgBirthFatherInfo;
import org.bel.birthdeath.birth.model.EgBirthMotherInfo;
import org.bel.birthdeath.birth.model.EgBirthPermaddr;
import org.bel.birthdeath.birth.model.EgBirthPresentaddr;
import org.bel.birthdeath.utils.CommonUtils;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class BirthDtlsAllRowMapper implements ResultSetExtractor<List<EgBirthDtl>> {
	
	@Autowired
	CommonUtils utils;

	@Override
	public List<EgBirthDtl> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, EgBirthDtl> birthDtlMap = new LinkedHashMap<>();
		try {
			while (rs.next()) {
				String birthdtlid = rs.getString("birthdtlid");
				EgBirthDtl birthDtl = birthDtlMap.get(birthdtlid);

				if (birthDtl == null) {
					EgBirthMotherInfo motherInfo = EgBirthMotherInfo.builder()
							.firstname(null!=rs.getString("bmotfn")?rs.getString("bmotfn").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.middlename(null!=rs.getString("bmotmn")?rs.getString("bmotmn").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.lastname(null!=rs.getString("bmotln")?rs.getString("bmotln").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.aadharno(null!=rs.getString("bmotaadharno")?rs.getString("bmotaadharno").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null).build();
					motherInfo.setFullName(utils.addfullName(motherInfo.getFirstname(),motherInfo.getMiddlename(),motherInfo.getLastname()));
					
					EgBirthFatherInfo fatherInfo = EgBirthFatherInfo.builder()
							.firstname(null!=rs.getString("bfatfn")?rs.getString("bfatfn").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.middlename(null!=rs.getString("bfatmn")?rs.getString("bfatmn").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.lastname(null!=rs.getString("bfatln")?rs.getString("bfatln").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.aadharno(null!=rs.getString("bfataadharno")?rs.getString("bfataadharno").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null).build();
					fatherInfo.setFullName(utils.addfullName(fatherInfo.getFirstname(),fatherInfo.getMiddlename(),fatherInfo.getLastname()));
					
					EgBirthPermaddr	permaddr = EgBirthPermaddr.builder()
							.houseno(null!=rs.getString("pmhouseno")?rs.getString("pmhouseno").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.buildingno(null!=rs.getString("pmbuildingno")?rs.getString("pmbuildingno").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.streetname(null!=rs.getString("pmstreetname")?rs.getString("pmstreetname").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.locality(null!=rs.getString("pmlocality")?rs.getString("pmlocality").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.tehsil(null!=rs.getString("pmtehsil")?rs.getString("pmtehsil").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.district(null!=rs.getString("pmdistrict")?rs.getString("pmdistrict").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.city(null!=rs.getString("pmcity")?rs.getString("pmcity").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.state(null!=rs.getString("pmstate")?rs.getString("pmstate").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.pinno(null!=rs.getString("pmpinno")?rs.getString("pmpinno").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.country(null!=rs.getString("pmcountry")?rs.getString("pmcountry").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null).build();
					permaddr.setFullAddress(utils.addFullAddress(permaddr.getHouseno(),permaddr.getBuildingno(),permaddr.getStreetname(),permaddr.getLocality(),permaddr.getTehsil(),
							permaddr.getDistrict(),permaddr.getCity(),permaddr.getState(),permaddr.getPinno(),permaddr.getCountry()));
					
					EgBirthPresentaddr presentaddr= EgBirthPresentaddr.builder()
							.houseno(null!=rs.getString("pshouseno")?rs.getString("pshouseno").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.buildingno(null!=rs.getString("psbuildingno")?rs.getString("psbuildingno").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.streetname(null!=rs.getString("psstreetname")?rs.getString("psstreetname").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.locality(null!=rs.getString("pslocality")?rs.getString("pslocality").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.tehsil(null!=rs.getString("pstehsil")?rs.getString("pstehsil").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.district(null!=rs.getString("psdistrict")?rs.getString("psdistrict").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.city(null!=rs.getString("pscity")?rs.getString("pscity").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.state(null!=rs.getString("psstate")?rs.getString("psstate").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.pinno(null!=rs.getString("pspinno")?rs.getString("pspinno").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.country(null!=rs.getString("pscountry")?rs.getString("pscountry").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null).build();
					presentaddr.setFullAddress(utils.addFullAddress(presentaddr.getHouseno(),presentaddr.getBuildingno(),presentaddr.getStreetname(),presentaddr.getLocality(),presentaddr.getTehsil(),
							presentaddr.getDistrict(),presentaddr.getCity(),presentaddr.getState(),presentaddr.getPinno(),presentaddr.getCountry()));
					
					birthDtl = EgBirthDtl.builder().id(birthdtlid)
							.registrationno(null!=rs.getString("registrationno")?rs.getString("registrationno").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.hospitalname(null!=rs.getString("hospitalname")?rs.getString("hospitalname").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.dateofreport(rs.getTimestamp("dateofreport")).gender(rs.getInt("gender"))
							.dateofbirth(rs.getTimestamp("dateofbirth")).counter(rs.getInt("counter")).genderStr(rs.getString("genderstr")).tenantid(rs.getString("tenantid")).dateofissue(System.currentTimeMillis())
							.firstname(null!=rs.getString("bdtlfn")?rs.getString("bdtlfn").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.middlename(null!=rs.getString("bdtlmn")?rs.getString("bdtlmn").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.lastname(null!=rs.getString("bdtlln")?rs.getString("bdtlln").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.birthMotherInfo(motherInfo).birthFatherInfo(fatherInfo)
							.birthPermaddr(permaddr).birthPresentaddr(presentaddr)
							.placeofbirth(null!=rs.getString("placeofbirth")?rs.getString("placeofbirth").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.remarks(null!=rs.getString("remarks")?rs.getString("remarks").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.build();
					birthDtl.setFullName(utils.addfullName(birthDtl.getFirstname(), birthDtl.getMiddlename(), birthDtl.getLastname()));
					birthDtlMap.put(birthdtlid, birthDtl);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException("INVALID INPUT", "Error in fetching data");
		}
		return new ArrayList<>(birthDtlMap.values());
	}
}

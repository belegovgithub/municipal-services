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
							.firstname(null!=rs.getString("bmotfn")?rs.getString("bmotfn").trim():rs.getString("bmotfn"))
							.middlename(null!=rs.getString("bmotmn")?rs.getString("bmotmn").trim():rs.getString("bmotmn"))
							.lastname(null!=rs.getString("bmotln")?rs.getString("bmotln").trim():rs.getString("bmotln"))
							.aadharno(null!=rs.getString("bmotaadharno")?rs.getString("bmotaadharno").trim():rs.getString("bmotaadharno")).build();
					motherInfo.setFullName(utils.addfullName(motherInfo.getFirstname(),motherInfo.getMiddlename(),motherInfo.getLastname()));
					
					EgBirthFatherInfo fatherInfo = EgBirthFatherInfo.builder()
							.firstname(null!=rs.getString("bfatfn")?rs.getString("bfatfn").trim():rs.getString("bfatfn"))
							.middlename(null!=rs.getString("bfatmn")?rs.getString("bfatmn").trim():rs.getString("bfatmn"))
							.lastname(null!=rs.getString("bfatln")?rs.getString("bfatln").trim():rs.getString("bfatln"))
							.aadharno(null!=rs.getString("bfataadharno")?rs.getString("bfataadharno").trim():rs.getString("bfataadharno")).build();
					fatherInfo.setFullName(utils.addfullName(fatherInfo.getFirstname(),fatherInfo.getMiddlename(),fatherInfo.getLastname()));
					
					EgBirthPermaddr	permaddr = EgBirthPermaddr.builder()
							.houseno(null!=rs.getString("pmhouseno")?rs.getString("pmhouseno").trim():rs.getString("pmhouseno"))
							.buildingno(null!=rs.getString("pmbuildingno")?rs.getString("pmbuildingno").trim():rs.getString("pmbuildingno"))
							.streetname(null!=rs.getString("pmstreetname")?rs.getString("pmstreetname").trim():rs.getString("pmstreetname"))
							.locality(null!=rs.getString("pmlocality")?rs.getString("pmlocality").trim():rs.getString("pmlocality"))
							.tehsil(null!=rs.getString("pmtehsil")?rs.getString("pmtehsil").trim():rs.getString("pmtehsil"))
							.district(null!=rs.getString("pmdistrict")?rs.getString("pmdistrict").trim():rs.getString("pmdistrict"))
							.city(null!=rs.getString("pmcity")?rs.getString("pmcity").trim():rs.getString("pmcity"))
							.state(null!=rs.getString("pmstate")?rs.getString("pmstate").trim():rs.getString("pmstate"))
							.pinno(null!=rs.getString("pmpinno")?rs.getString("pmpinno").trim():rs.getString("pmpinno"))
							.country(null!=rs.getString("pmcountry")?rs.getString("pmcountry").trim():rs.getString("pmcountry")).build();
					permaddr.setFullAddress(utils.addFullAddress(permaddr.getHouseno(),permaddr.getBuildingno(),permaddr.getStreetname(),permaddr.getLocality(),permaddr.getTehsil(),
							permaddr.getDistrict(),permaddr.getCity(),permaddr.getState(),permaddr.getPinno(),permaddr.getCountry()));
					
					EgBirthPresentaddr presentaddr= EgBirthPresentaddr.builder()
							.houseno(null!=rs.getString("pshouseno")?rs.getString("pshouseno").trim():rs.getString("pshouseno"))
							.buildingno(null!=rs.getString("psbuildingno")?rs.getString("psbuildingno").trim():rs.getString("psbuildingno"))
							.streetname(null!=rs.getString("psstreetname")?rs.getString("psstreetname").trim():rs.getString("psstreetname"))
							.locality(null!=rs.getString("pslocality")?rs.getString("pslocality").trim():rs.getString("pslocality"))
							.tehsil(null!=rs.getString("pstehsil")?rs.getString("pstehsil").trim():rs.getString("pstehsil"))
							.district(null!=rs.getString("psdistrict")?rs.getString("psdistrict").trim():rs.getString("psdistrict"))
							.city(null!=rs.getString("pscity")?rs.getString("pscity").trim():rs.getString("pscity"))
							.state(null!=rs.getString("psstate")?rs.getString("psstate").trim():rs.getString("psstate"))
							.pinno(null!=rs.getString("pspinno")?rs.getString("pspinno").trim():rs.getString("pspinno"))
							.country(null!=rs.getString("pscountry")?rs.getString("pscountry").trim():rs.getString("pscountry")).build();
					presentaddr.setFullAddress(utils.addFullAddress(presentaddr.getHouseno(),presentaddr.getBuildingno(),presentaddr.getStreetname(),presentaddr.getLocality(),presentaddr.getTehsil(),
							presentaddr.getDistrict(),presentaddr.getCity(),presentaddr.getState(),presentaddr.getPinno(),presentaddr.getCountry()));
					
					birthDtl = EgBirthDtl.builder().id(birthdtlid)
							.registrationno(null!=rs.getString("registrationno")?rs.getString("registrationno").trim():rs.getString("registrationno"))
							.hospitalname(null!=rs.getString("hospitalname")?rs.getString("hospitalname").trim():rs.getString("hospitalname"))
							.dateofreport(rs.getTimestamp("dateofreport")).gender(rs.getInt("gender"))
							.dateofbirth(rs.getTimestamp("dateofbirth")).counter(rs.getInt("counter")).genderStr(rs.getString("genderstr")).tenantid(rs.getString("tenantid")).dateofissue(System.currentTimeMillis())
							.firstname(null!=rs.getString("bdtlfn")?rs.getString("bdtlfn").trim():rs.getString("bdtlfn"))
							.middlename(null!=rs.getString("bdtlmn")?rs.getString("bdtlmn").trim():rs.getString("bdtlmn"))
							.lastname(null!=rs.getString("bdtlln")?rs.getString("bdtlln").trim():rs.getString("bdtlln"))
							.birthMotherInfo(motherInfo).birthFatherInfo(fatherInfo)
							.birthPermaddr(permaddr).birthPresentaddr(presentaddr)
							.placeofbirth(null!=rs.getString("placeofbirth")?rs.getString("placeofbirth").trim():rs.getString("placeofbirth"))
							.remarks(null!=rs.getString("remarks")?rs.getString("remarks").trim():rs.getString("remarks"))
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

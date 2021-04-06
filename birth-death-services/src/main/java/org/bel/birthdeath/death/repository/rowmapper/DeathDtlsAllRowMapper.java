package org.bel.birthdeath.death.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bel.birthdeath.death.model.EgDeathDtl;
import org.bel.birthdeath.death.model.EgDeathFatherInfo;
import org.bel.birthdeath.death.model.EgDeathMotherInfo;
import org.bel.birthdeath.death.model.EgDeathPermaddr;
import org.bel.birthdeath.death.model.EgDeathPresentaddr;
import org.bel.birthdeath.death.model.EgDeathSpouseInfo;
import org.bel.birthdeath.utils.CommonUtils;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class DeathDtlsAllRowMapper implements ResultSetExtractor<List<EgDeathDtl>> {

	@Autowired
	CommonUtils utils;
	
	@Override
	public List<EgDeathDtl> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, EgDeathDtl> deathDtlMap = new LinkedHashMap<>();
		try {
			while (rs.next()) {
				String deathdtlid = rs.getString("deathdtlid");
				EgDeathDtl deathDtl = deathDtlMap.get(deathdtlid);

				if (deathDtl == null) {
					EgDeathMotherInfo motherInfo = EgDeathMotherInfo.builder()
							.firstname(null!=rs.getString("bmotfn")?rs.getString("bmotfn").trim():rs.getString("bmotfn"))
							.middlename(null!=rs.getString("bmotmn")?rs.getString("bmotmn").trim():rs.getString("bmotmn"))
							.lastname(null!=rs.getString("bmotln")?rs.getString("bmotln").trim():rs.getString("bmotln"))
							.aadharno(null!=rs.getString("bmotaadharno")?rs.getString("bmotaadharno").trim():rs.getString("bmotaadharno")).build();
					motherInfo.setFullName(utils.addfullName(motherInfo.getFirstname(),motherInfo.getMiddlename(),motherInfo.getLastname()));
					
					EgDeathFatherInfo fatherInfo = EgDeathFatherInfo.builder()
							.firstname(null!=rs.getString("bfatfn")?rs.getString("bfatfn").trim():rs.getString("bfatfn"))
							.middlename(null!=rs.getString("bfatmn")?rs.getString("bfatmn").trim():rs.getString("bfatmn"))
							.lastname(null!=rs.getString("bfatln")?rs.getString("bfatln").trim():rs.getString("bfatln"))
							.aadharno(null!=rs.getString("bfataadharno")?rs.getString("bfataadharno").trim():rs.getString("bfataadharno")).build();
					fatherInfo.setFullName(utils.addfullName(fatherInfo.getFirstname(),fatherInfo.getMiddlename(),fatherInfo.getLastname()));
					
					EgDeathSpouseInfo spouseInfo = EgDeathSpouseInfo.builder()
							.firstname(null!=rs.getString("bspsfn")?rs.getString("bspsfn").trim():rs.getString("bspsfn"))
							.middlename(null!=rs.getString("bspsmn")?rs.getString("bspsmn").trim():rs.getString("bspsmn"))
							.lastname(null!=rs.getString("bspsln")?rs.getString("bspsln").trim():rs.getString("bspsln"))
							.aadharno(null!=rs.getString("bspsaadharno")?rs.getString("bspsaadharno").trim():rs.getString("bspsaadharno")).build();
					spouseInfo.setFullName(utils.addfullName(spouseInfo.getFirstname(),spouseInfo.getMiddlename(),spouseInfo.getLastname()));
					
					EgDeathPermaddr	permaddr = EgDeathPermaddr.builder()
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
					
					EgDeathPresentaddr presentaddr= EgDeathPresentaddr.builder()
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
					
					deathDtl = EgDeathDtl.builder().id(deathdtlid)
							.registrationno(null!=rs.getString("registrationno")?rs.getString("registrationno").trim():rs.getString("registrationno"))
							.hospitalname(null!=rs.getString("hospitalname")?rs.getString("hospitalname").trim():rs.getString("hospitalname"))
							.dateofreport(rs.getTimestamp("dateofreport")).gender(rs.getInt("gender"))
							.dateofdeath(rs.getTimestamp("dateofdeath")).counter(rs.getInt("counter")).genderStr(rs.getString("genderstr")).tenantid(rs.getString("tenantid")).dateofissue(System.currentTimeMillis())
							.firstname(null!=rs.getString("bdtlfn")?rs.getString("bdtlfn").trim():rs.getString("bdtlfn"))
							.middlename(null!=rs.getString("bdtlmn")?rs.getString("bdtlmn").trim():rs.getString("bdtlmn"))
							.lastname(null!=rs.getString("bdtlln")?rs.getString("bdtlln").trim():rs.getString("bdtlln"))
							.deathMotherInfo(motherInfo).deathFatherInfo(fatherInfo).deathSpouseInfo(spouseInfo)
							.deathPermaddr(permaddr).deathPresentaddr(presentaddr)
							.placeofdeath(null!=rs.getString("placeofdeath")?rs.getString("placeofdeath").trim():rs.getString("placeofdeath"))
							.remarks(null!=rs.getString("remarks")?rs.getString("remarks").trim():rs.getString("remarks"))
							.age(rs.getString("age").trim())
							.aadharno(null!=rs.getString("bdtlaadharno")?rs.getString("bdtlaadharno").trim():rs.getString("bdtlaadharno")).build();
					deathDtl.setFullName(utils.addfullName(deathDtl.getFirstname(), deathDtl.getMiddlename(), deathDtl.getLastname()));
					deathDtlMap.put(deathdtlid, deathDtl);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException("INVALID INPUT", "Error in fetching data");
		}
		return new ArrayList<>(deathDtlMap.values());
	}
}

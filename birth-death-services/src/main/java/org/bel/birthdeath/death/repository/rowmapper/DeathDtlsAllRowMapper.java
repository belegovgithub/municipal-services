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
				System.out.println("rs.getString(bdtlfn) "+rs.getString("bdtlfn"));
				if (deathDtl == null) {
					EgDeathMotherInfo motherInfo = EgDeathMotherInfo.builder()
							.firstname(null!=rs.getString("bmotfn")?rs.getString("bmotfn").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.middlename(null!=rs.getString("bmotmn")?rs.getString("bmotmn").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.lastname(null!=rs.getString("bmotln")?rs.getString("bmotln").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.aadharno(null!=rs.getString("bmotaadharno")?rs.getString("bmotaadharno").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null).build();
					motherInfo.setFullName(utils.addfullName(motherInfo.getFirstname(),motherInfo.getMiddlename(),motherInfo.getLastname()));
					
					EgDeathFatherInfo fatherInfo = EgDeathFatherInfo.builder()
							.firstname(null!=rs.getString("bfatfn")?rs.getString("bfatfn").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.middlename(null!=rs.getString("bfatmn")?rs.getString("bfatmn").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.lastname(null!=rs.getString("bfatln")?rs.getString("bfatln").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.aadharno(null!=rs.getString("bfataadharno")?rs.getString("bfataadharno").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null).build();
					fatherInfo.setFullName(utils.addfullName(fatherInfo.getFirstname(),fatherInfo.getMiddlename(),fatherInfo.getLastname()));
					
					EgDeathSpouseInfo spouseInfo = EgDeathSpouseInfo.builder()
							.firstname(null!=rs.getString("bspsfn")?rs.getString("bspsfn").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.middlename(null!=rs.getString("bspsmn")?rs.getString("bspsmn").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.lastname(null!=rs.getString("bspsln")?rs.getString("bspsln").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.aadharno(null!=rs.getString("bspsaadharno")?rs.getString("bspsaadharno").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null).build();
					spouseInfo.setFullName(utils.addfullName(spouseInfo.getFirstname(),spouseInfo.getMiddlename(),spouseInfo.getLastname()));
					
					EgDeathPermaddr	permaddr = EgDeathPermaddr.builder()
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
					
					EgDeathPresentaddr presentaddr= EgDeathPresentaddr.builder()
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
					
					deathDtl = EgDeathDtl.builder().id(deathdtlid)
							.registrationno(null!=rs.getString("registrationno")?rs.getString("registrationno").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.hospitalname(null!=rs.getString("hospitalname")?rs.getString("hospitalname").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.dateofreport(rs.getTimestamp("dateofreport")).gender(rs.getInt("gender"))
							.dateofdeath(rs.getTimestamp("dateofdeath")).counter(rs.getInt("counter")).genderStr(rs.getString("genderstr")).tenantid(rs.getString("tenantid")).dateofissue(System.currentTimeMillis())
							.firstname(null!=rs.getString("bdtlfn")?rs.getString("bdtlfn").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.middlename(null!=rs.getString("bdtlmn")?rs.getString("bdtlmn").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.lastname(null!=rs.getString("bdtlln")?rs.getString("bdtlln").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.deathMotherInfo(motherInfo).deathFatherInfo(fatherInfo).deathSpouseInfo(spouseInfo)
							.deathPermaddr(permaddr).deathPresentaddr(presentaddr)
							.placeofdeath(null!=rs.getString("placeofdeath")?rs.getString("placeofdeath").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.remarks(null!=rs.getString("remarks")?rs.getString("remarks").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null)
							.age(rs.getString("age").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"))
							.aadharno(null!=rs.getString("bdtlaadharno")?rs.getString("bdtlaadharno").replaceAll("(\\r|\\n|\\t)", "").replace("\"","\'"):null).build();
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

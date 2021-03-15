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
import org.egov.tracer.model.CustomException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class BirthDtlsAllRowMapper implements ResultSetExtractor<List<EgBirthDtl>> {

	@Override
	public List<EgBirthDtl> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, EgBirthDtl> birthDtlMap = new LinkedHashMap<>();
		try {
			while (rs.next()) {
				String birthdtlid = rs.getString("birthdtlid");
				EgBirthDtl birthDtl = birthDtlMap.get(birthdtlid);

				if (birthDtl == null) {
					EgBirthMotherInfo motherInfo = EgBirthMotherInfo.builder().firstname(rs.getString("bmotfn")).middlename(rs.getString("bmotmn")).lastname(rs.getString("bmotln"))
							.build();
					motherInfo.setFullName(addfullName(motherInfo.getFirstname(),motherInfo.getMiddlename(),motherInfo.getLastname()));
					
					EgBirthFatherInfo fatherInfo = EgBirthFatherInfo.builder().firstname(rs.getString("bfatfn")).middlename(rs.getString("bfatmn")).lastname(rs.getString("bfatln"))
							.build();
					fatherInfo.setFullName(addfullName(fatherInfo.getFirstname(),fatherInfo.getMiddlename(),fatherInfo.getLastname()));
					
					EgBirthPermaddr	permaddr = EgBirthPermaddr.builder().houseno(rs.getString("pmhouseno")).buildingno(rs.getString("pmbuildingno"))
							.streetname(rs.getString("pmstreetname")).locality(rs.getString("pmlocality")).tehsil(rs.getString("pmtehsil")).district(rs.getString("pmdistrict"))
							.city(rs.getString("pmcity")).state(rs.getString("pmstate")).pinno(rs.getString("pmpinno")).country(rs.getString("pmcountry")).build();
					permaddr.setFullAddress(addFullAddress(permaddr.getHouseno(),permaddr.getBuildingno(),permaddr.getStreetname(),permaddr.getLocality(),permaddr.getTehsil(),
							permaddr.getDistrict(),permaddr.getCity(),permaddr.getState(),permaddr.getPinno(),permaddr.getCountry()));
					
					EgBirthPresentaddr presentaddr= EgBirthPresentaddr.builder().houseno(rs.getString("pshouseno")).buildingno(rs.getString("psbuildingno"))
							.streetname(rs.getString("psstreetname")).locality(rs.getString("pslocality")).tehsil(rs.getString("pstehsil")).district(rs.getString("psdistrict"))
							.city(rs.getString("pscity")).state(rs.getString("psstate")).pinno(rs.getString("pspinno")).country(rs.getString("pscountry")).build();
					presentaddr.setFullAddress(addFullAddress(presentaddr.getHouseno(),presentaddr.getBuildingno(),presentaddr.getStreetname(),presentaddr.getLocality(),presentaddr.getTehsil(),
							presentaddr.getDistrict(),presentaddr.getCity(),presentaddr.getState(),presentaddr.getPinno(),presentaddr.getCountry()));
					
					birthDtl = EgBirthDtl.builder().id(birthdtlid).registrationno(rs.getString("registrationno")).hospitalname(rs.getString("hospitalname")).dateofreport(rs.getTimestamp("dateofreport")).gender(rs.getInt("gender"))
							.dateofbirth(rs.getTimestamp("dateofbirth")).counter(rs.getInt("counter")).genderStr(rs.getString("genderstr")).tenantid(rs.getString("tenantid")).dateofissue(System.currentTimeMillis())
							.firstname(rs.getString("bdtlfn")).middlename(rs.getString("bdtlmn")).lastname(rs.getString("bdtlln")).birthMotherInfo(motherInfo).birthFatherInfo(fatherInfo)
							.birthPermaddr(permaddr).birthPresentaddr(presentaddr)
							.build();
					birthDtl.setFullName(addfullName(birthDtl.getFirstname(), birthDtl.getMiddlename(), birthDtl.getLastname()));
					birthDtlMap.put(birthdtlid, birthDtl);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException("INVALID INPUT", "Error in fetching data");
		}
		return new ArrayList<>(birthDtlMap.values());
	}

	private String addfullName(String firstname, String middlename, String lastname) {
		StringBuilder fullName = new StringBuilder();
		if(null!=firstname)
			fullName.append(firstname);
		if(null!=middlename)
			fullName.append(" "+middlename);
		if(null!=lastname)
			fullName.append(" "+lastname);
		return fullName.toString();
	}

	private String addFullAddress(String houseno, String buildingno, String streetname, String locality, String tehsil,
			String district, String city, String state, String pinno, String country) {
		StringBuilder fullAddress = new StringBuilder();
		if(null!=houseno)
			fullAddress.append(houseno);
		if(null!=buildingno)
			fullAddress.append(" "+buildingno);
		if(null!=streetname)
			fullAddress.append(" "+streetname);
		if(null!=locality)
			fullAddress.append(" "+locality);
		if(null!=tehsil)
			fullAddress.append(" "+tehsil);
		if(null!=district)
			fullAddress.append(" "+district);
		if(null!=city)
			fullAddress.append(" "+city);
		if(null!=state)
			fullAddress.append(" "+state);
		if(null!=pinno)
			fullAddress.append(" "+pinno);
		if(null!=country)
			fullAddress.append(" "+country);
		return fullAddress.toString();
	}

}

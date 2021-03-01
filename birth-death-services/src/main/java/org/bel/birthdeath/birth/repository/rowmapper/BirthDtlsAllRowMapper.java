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
					EgBirthFatherInfo fatherInfo = EgBirthFatherInfo.builder().firstname(rs.getString("bfatfn")).middlename(rs.getString("bfatmn")).lastname(rs.getString("bfatln"))
							.build();
					EgBirthPermaddr	permaddr = EgBirthPermaddr.builder().fullAddress(rs.getString("permaddress")).build();
					EgBirthPresentaddr presentaddr= EgBirthPresentaddr.builder().fullAddress(rs.getString("presentaddress")).build();
					birthDtl = EgBirthDtl.builder().id(birthdtlid).registrationno(rs.getString("registrationno"))
							.dateofbirth(rs.getTimestamp("dateofbirth")).counter(rs.getInt("counter")).gender(rs.getInt("gender")).tenantid(rs.getString("tenantid"))
							.firstname(rs.getString("bdtlfn")).birthMotherInfo(motherInfo).birthFatherInfo(fatherInfo)
							.birthPermaddr(permaddr).birthPresentaddr(presentaddr)
							.build();
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

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
import org.egov.tracer.model.CustomException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class BirthDtlsRowMapper implements ResultSetExtractor<List<EgBirthDtl>> {

	@Override
	public List<EgBirthDtl> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, EgBirthDtl> birthDtlMap = new LinkedHashMap<>();
		try {
			while (rs.next()) {
				String regNo = rs.getString("registrationno");
				EgBirthDtl birthDtl = birthDtlMap.get(regNo);

				if (birthDtl == null) {
					EgBirthMotherInfo motherInfo = EgBirthMotherInfo.builder().firstname(rs.getString("bmotfn"))
							.build();
					EgBirthFatherInfo fatherInfo = EgBirthFatherInfo.builder().firstname(rs.getString("bfatfn"))
							.build();
					birthDtl = EgBirthDtl.builder().registrationno(rs.getString("registrationno"))
							.dateofbirth(rs.getTimestamp("dateofbirth")).counter(rs.getInt("counter"))
							.firstname(rs.getString("bdtlfn")).birthMotherInfo(motherInfo).birthFatherInfo(fatherInfo)
							.build();
					birthDtlMap.put(regNo, birthDtl);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException("INVALID INPUT", "Error in fetching data");
		}
		return new ArrayList<>(birthDtlMap.values());
	}

}

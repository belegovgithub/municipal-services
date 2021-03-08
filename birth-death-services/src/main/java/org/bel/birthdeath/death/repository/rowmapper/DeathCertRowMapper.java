package org.bel.birthdeath.death.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bel.birthdeath.death.certmodel.DeathCertificate;
import org.egov.tracer.model.CustomException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class DeathCertRowMapper implements ResultSetExtractor<List<DeathCertificate>> {

	@Override
	public List<DeathCertificate> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, DeathCertificate> deathDtlMap = new LinkedHashMap<>();
		try {
			while (rs.next()) {
				String id = rs.getString("id");
				DeathCertificate certReq = deathDtlMap.get(id);

				if (certReq == null) {
					certReq = DeathCertificate.builder().id(id).filestoreid(rs.getString("filestoreid"))
							.deathDtlId(rs.getString("deathdtlid"))
							.build();
					deathDtlMap.put(id, certReq);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException("INVALID INPUT", "Error in fetching data");
		}
		return new ArrayList<DeathCertificate> (deathDtlMap.values());
	}

}

package org.bel.birthdeath.death.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bel.birthdeath.utils.BirthDeathConstants;
import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class ImportDeathWrapper {
	
    @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo = null;

    @JsonProperty("statsMap")
	private Map<String,Integer> statsMap;
	
    @JsonProperty("statsMapData")
	private Map<String,List<EgDeathDtl>> statsMapData;
	
	public ImportDeathWrapper() {
		statsMap =  new HashMap<String, Integer>();
		statsMap.put(BirthDeathConstants.TENANT_EMPTY,0);
		statsMap.put(BirthDeathConstants.MANDATORY_MISSING,0);
		statsMap.put(BirthDeathConstants.DUPLICATE_REG,0);
		statsMap.put(BirthDeathConstants.REG_EMPTY,0);
		statsMap.put(BirthDeathConstants.DOD_EMPTY,0);
		statsMap.put(BirthDeathConstants.GENDER_EMPTY,0);
		statsMap.put(BirthDeathConstants.GENDER_INVALID,0);
		statsMap.put(BirthDeathConstants.FIRSTNAME_LARGE,0);
		statsMap.put(BirthDeathConstants.MIDDLENAME_LARGE,0);
		statsMap.put(BirthDeathConstants.LASTNAME_LARGE,0);
		statsMap.put(BirthDeathConstants.F_FIRSTNAME_LARGE,0);
		statsMap.put(BirthDeathConstants.F_MIDDLENAME_LARGE,0);
		statsMap.put(BirthDeathConstants.F_LASTNAME_LARGE,0);
		statsMap.put(BirthDeathConstants.M_FIRSTNAME_LARGE,0);
		statsMap.put(BirthDeathConstants.M_MIDDLENAME_LARGE,0);
		statsMap.put(BirthDeathConstants.M_LASTNAME_LARGE,0);
		statsMap.put(BirthDeathConstants.DUPLICATE_REG_EXCEL,0);

		statsMapData =  new HashMap<String, List<EgDeathDtl>>();
		statsMapData.put(BirthDeathConstants.TENANT_EMPTY,new ArrayList<EgDeathDtl>());
		statsMapData.put(BirthDeathConstants.MANDATORY_MISSING,new ArrayList<EgDeathDtl>());
		statsMapData.put(BirthDeathConstants.DUPLICATE_REG,new ArrayList<EgDeathDtl>());
		statsMapData.put(BirthDeathConstants.REG_EMPTY,new ArrayList<EgDeathDtl>());
		statsMapData.put(BirthDeathConstants.DOD_EMPTY,new ArrayList<EgDeathDtl>());
		statsMapData.put(BirthDeathConstants.GENDER_EMPTY,new ArrayList<EgDeathDtl>());
		statsMapData.put(BirthDeathConstants.GENDER_INVALID,new ArrayList<EgDeathDtl>());
		statsMapData.put(BirthDeathConstants.FIRSTNAME_LARGE,new ArrayList<EgDeathDtl>());
		statsMapData.put(BirthDeathConstants.MIDDLENAME_LARGE,new ArrayList<EgDeathDtl>());
		statsMapData.put(BirthDeathConstants.LASTNAME_LARGE,new ArrayList<EgDeathDtl>());
		statsMapData.put(BirthDeathConstants.F_FIRSTNAME_LARGE,new ArrayList<EgDeathDtl>());
		statsMapData.put(BirthDeathConstants.F_MIDDLENAME_LARGE,new ArrayList<EgDeathDtl>());
		statsMapData.put(BirthDeathConstants.F_LASTNAME_LARGE,new ArrayList<EgDeathDtl>());
		statsMapData.put(BirthDeathConstants.M_FIRSTNAME_LARGE,new ArrayList<EgDeathDtl>());
		statsMapData.put(BirthDeathConstants.M_MIDDLENAME_LARGE,new ArrayList<EgDeathDtl>());
		statsMapData.put(BirthDeathConstants.M_LASTNAME_LARGE,new ArrayList<EgDeathDtl>());
		statsMapData.put(BirthDeathConstants.DUPLICATE_REG_EXCEL,new ArrayList<EgDeathDtl>());
	}
	
	public void updateMaps(String error,EgDeathDtl record)
	{
		statsMap.put(error,statsMap.get(error)+1);
		statsMapData.get(error).add(record);
	}

	public void finaliseStats(int total, int success) {
		int failed = 0;
		for (String key : statsMap.keySet()) {
			failed = failed + statsMap.get(key);
		}
		statsMap.put("Total Records",total);
		statsMap.put("Sucessful Records",success);
		statsMap.put("Failed Records",failed);
	}
}

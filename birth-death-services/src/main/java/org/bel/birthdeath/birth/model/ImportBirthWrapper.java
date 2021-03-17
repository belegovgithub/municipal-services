package org.bel.birthdeath.birth.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bel.birthdeath.utils.BirthDeathConstants;
import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class ImportBirthWrapper {
	
    @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo = null;

    @JsonProperty("statsMap")
	private Map<String,Integer> statsMap = new HashMap<String, Integer>();;
	
    @JsonProperty("statsMapData")
	private Map<String,List<EgBirthDtl>> statsMapData =  new HashMap<String, List<EgBirthDtl>>();
    
    @JsonProperty("errorRowMap")
   	private Map<String,List<String>> errorRowMap =  new HashMap<String, List<String>>();
    
	List<String> keyList = Arrays.asList(new String[] { 
			BirthDeathConstants.TENANT_EMPTY,
			BirthDeathConstants.MANDATORY_MISSING,
			BirthDeathConstants.DUPLICATE_REG,
			BirthDeathConstants.REG_EMPTY,
			BirthDeathConstants.DOB_EMPTY,
			BirthDeathConstants.GENDER_EMPTY,
			BirthDeathConstants.GENDER_INVALID,
			BirthDeathConstants.FIRSTNAME_LARGE,
			BirthDeathConstants.MIDDLENAME_LARGE,
			BirthDeathConstants.LASTNAME_LARGE,
			BirthDeathConstants.F_FIRSTNAME_LARGE,
			BirthDeathConstants.F_MIDDLENAME_LARGE,
			BirthDeathConstants.F_LASTNAME_LARGE,
			BirthDeathConstants.M_FIRSTNAME_LARGE,
			BirthDeathConstants.M_MIDDLENAME_LARGE,
			BirthDeathConstants.M_LASTNAME_LARGE,
			BirthDeathConstants.DUPLICATE_REG_EXCEL,
			BirthDeathConstants.INVALID_DOB,
			BirthDeathConstants.INVALID_DOB_RANGE,
			BirthDeathConstants.INVALID_DOR,
			BirthDeathConstants.INVALID_DOR_RANGE
			});
    
	public ImportBirthWrapper() {
		for (String key : keyList) {
			statsMap.put(key,0);
			statsMapData.put(key,new ArrayList<EgBirthDtl>());
			errorRowMap.put(key,new ArrayList<String>());
		}
	}
	
	public void updateMaps(String error,EgBirthDtl record)
	{
		statsMap.put(error,statsMap.get(error)+1);
		statsMapData.get(error).add(record);
		errorRowMap.get(error).add(record.getExcelrowindex());
	}

	public void finaliseStats(int total, int success) {
		int failed = 0;
		for (String key : statsMap.keySet()) {
			if(key.equals(BirthDeathConstants.DUPLICATE_REG_EXCEL))
			{
				failed = failed + statsMap.get(key)/2;
			}
			else
			{
				failed = failed + statsMap.get(key);
			}
		}
		for (String key : keyList) {
			if(statsMap.get(key)==0)
			{
				statsMap.remove(key);
				statsMapData.remove(key);
				errorRowMap.remove(key);
			}
		}
		statsMap.put("Total Records",total);
		statsMap.put("Sucessful Records",success);
		statsMap.put("Failed Records",failed);
	}
}

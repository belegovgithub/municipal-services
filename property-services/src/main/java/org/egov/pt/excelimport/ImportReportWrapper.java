package org.egov.pt.excelimport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ImportReportWrapper {

	final static String successReport = "Successful : ";
	final static String propertyNotFoundReport = "Property not found : ";
	final static String taxCodeNotFoundReport = "Tax Head code not found row : ";
	final static String issueFoundReport = "Issue in row-column : ";

	@JsonIgnore
	List<String> keyList = Arrays
			.asList(new String[] { successReport, propertyNotFoundReport, taxCodeNotFoundReport, issueFoundReport });

	@JsonProperty("statsMap")
	private Map<String, Integer> statsMap;

	@JsonProperty("statsMapData")
	private Map<String, List<String>> statsMapData;

	public ImportReportWrapper() {
		statsMap = new HashMap<String, Integer>();
		statsMapData = new HashMap<String, List<String>>();
		for (String key : keyList) {
			statsMap.put(key, 0);
			statsMapData.put(key, new ArrayList<String>());
		}
	}

	public void updateMaps(String status, String data) {
		statsMap.put(status, statsMap.get(status) + 1);
		statsMapData.get(status).add(data);

	}
}

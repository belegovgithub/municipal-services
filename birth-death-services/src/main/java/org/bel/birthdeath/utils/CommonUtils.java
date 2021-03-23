package org.bel.birthdeath.utils;


import org.bel.birthdeath.common.model.AuditDetails;
import org.springframework.stereotype.Component;

import lombok.Getter;
@Component
@Getter
public class CommonUtils {
	

    /**
     * Method to return auditDetails for create/update flows
     *
     * @param by
     * @param isCreate
     * @return AuditDetails
     */
    public AuditDetails getAuditDetails(String by, Boolean isCreate) {
    	
        Long time = System.currentTimeMillis();
        
        if(isCreate)
            return AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time).build();
        else
            return AuditDetails.builder().lastModifiedBy(by).lastModifiedTime(time).build();
    }
    
    public String addfullName(String firstname, String middlename, String lastname) {
		StringBuilder fullName = new StringBuilder();
		if(null!=firstname)
			fullName.append(firstname);
		if(null!=middlename)
			fullName.append(" "+middlename);
		if(null!=lastname)
			fullName.append(" "+lastname);
		return fullName.toString();
	}

	public String addFullAddress(String houseno, String buildingno, String streetname, String locality, String tehsil,
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

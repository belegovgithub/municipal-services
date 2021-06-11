package org.bel.birthdeath.common.services;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.bel.birthdeath.birth.model.ImportBirthWrapper;
import org.bel.birthdeath.common.contract.BirthResponse;
import org.bel.birthdeath.common.contract.DeathResponse;
import org.bel.birthdeath.common.model.DropDownData;
import org.bel.birthdeath.common.model.EgHospitalDtl;
import org.bel.birthdeath.common.model.EmpDeclUtilResponse;
import org.bel.birthdeath.common.model.EmpDeclarationDtls;
import org.bel.birthdeath.common.repository.CommonRepository;
import org.bel.birthdeath.death.model.ImportDeathWrapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CommonService {
	
	@Autowired
	CommonRepository repository;
	
	@Value("${egov.empdecl.year}")
    private Integer startYear;
	
	public List<EgHospitalDtl> search(String tenantId) {
		List<EgHospitalDtl> hospitalDtls = new ArrayList<EgHospitalDtl>() ;
		hospitalDtls = repository.getHospitalDtls(tenantId);
		return hospitalDtls;
	}

	public ImportBirthWrapper saveBirthImport(BirthResponse importJSon, RequestInfo requestInfo) {
		ImportBirthWrapper importBirthWrapper = repository.saveBirthImport(importJSon, requestInfo);
		return importBirthWrapper;
	}
	
	public ImportDeathWrapper saveDeathImport(DeathResponse importJSon, RequestInfo requestInfo) {
		ImportDeathWrapper importDeathWrapper = repository.saveDeathImport(importJSon, requestInfo);
		return importDeathWrapper;
	}

	public ImportBirthWrapper updateBirthImport(BirthResponse importJSon, RequestInfo requestInfo) {
		ImportBirthWrapper importBirthWrapper = repository.updateBirthImport(importJSon, requestInfo);
		return importBirthWrapper;
	}
	
	public ImportDeathWrapper updateDeathImport(DeathResponse importJSon, RequestInfo requestInfo) {
		ImportDeathWrapper importDeathWrapper = repository.updateDeathImport(importJSon, requestInfo);
		return importDeathWrapper;
	}

	public int deleteBirthImport(String tenantId, RequestInfo requestInfo) {
		return repository.deleteBirthImport(tenantId,requestInfo);
	}
	
	public int deleteDeathImport(String tenantId, RequestInfo requestInfo) {
		return repository.deleteDeathImport(tenantId,requestInfo);
	}

	public EmpDeclarationDtls checkDeclaration(String tenantId) {
		return repository.checkDeclaration(tenantId);
	}

	public String updateDeclaration(EmpDeclarationDtls declarationDtls) {
		if(null==declarationDtls.getAgreed() || null==declarationDtls.getStartdateepoch() || declarationDtls.getStartdateepoch().isEmpty() 
				|| null==declarationDtls.getEnddateepoch() || declarationDtls.getEnddateepoch().isEmpty() )
			throw new CustomException("INVALID_DATA","Mandatory Fields cannot be empty");
		return repository.updateDeclaration(declarationDtls);
	}

	public EmpDeclUtilResponse getYears() {
		try {
			EmpDeclUtilResponse response = new EmpDeclUtilResponse();
			ArrayList<DropDownData> datas = new ArrayList<DropDownData>();
			int endYear=Calendar.getInstance().get(Calendar.YEAR);
			for(Integer i = startYear;i <= endYear; i++) {
				DropDownData data = new DropDownData();
				data.setOption(i.toString());
				data.setValue(i.toString());
				datas.add(data);
			}
			response.setYears(datas);
			return response;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public EmpDeclUtilResponse getMonths() {
		try {
			EmpDeclUtilResponse response = new EmpDeclUtilResponse();
			ArrayList<DropDownData> datas = new ArrayList<DropDownData>();
			String[] monthsArr = new DateFormatSymbols().getShortMonths();
			for(Integer i=0; i<12; i++) {
				DropDownData data = new DropDownData();
				data.setOption(String.valueOf(i+1));
				data.setValue(monthsArr[i]);
				datas.add(data);
			}
			response.setMonths(datas);
			return response;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

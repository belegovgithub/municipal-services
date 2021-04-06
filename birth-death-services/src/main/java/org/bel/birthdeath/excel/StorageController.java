package org.bel.birthdeath.excel;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.bel.birthdeath.birth.model.EgBirthDtl;
import org.bel.birthdeath.birth.model.EgBirthFatherInfo;
import org.bel.birthdeath.birth.model.EgBirthMotherInfo;
import org.bel.birthdeath.birth.model.EgBirthPermaddr;
import org.bel.birthdeath.birth.model.EgBirthPresentaddr;
import org.bel.birthdeath.birth.model.ImportBirthWrapper;
import org.bel.birthdeath.common.contract.BirthResponse;
import org.bel.birthdeath.common.contract.DeathResponse;
import org.bel.birthdeath.common.contract.RequestInfoWrapper;
import org.bel.birthdeath.common.services.CommonService;
import org.bel.birthdeath.death.model.EgDeathDtl;
import org.bel.birthdeath.death.model.EgDeathFatherInfo;
import org.bel.birthdeath.death.model.EgDeathMotherInfo;
import org.bel.birthdeath.death.model.EgDeathPermaddr;
import org.bel.birthdeath.death.model.EgDeathPresentaddr;
import org.bel.birthdeath.death.model.EgDeathSpouseInfo;
import org.bel.birthdeath.death.model.ImportDeathWrapper;
import org.bel.birthdeath.utils.ResponseInfoFactory;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
@Controller
@RequestMapping("/upload")
@Slf4j
public class StorageController {
	
	@Autowired
	CommonService commonService;
	
	@Autowired
	private ResponseInfoFactory responseInfoFactory;
	
	@Value("${egov.bnd.excelimport.flag}")
    private boolean excelImportFlag;
	
	@RequestMapping(value = { "/_birth"}, method = RequestMethod.POST)
    @PostMapping(produces = APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<ImportBirthWrapper> uploadBirth(@RequestParam(value = "file", required = false) List<MultipartFile> files,
            @RequestParam(value = "tenantId" , required = false) String tenantId) {
		if(excelImportFlag)	{
        String extension = "";
        Path testFile = null;
        BirthResponse importJSon = new BirthResponse();
        List<EgBirthDtl> birthCerts = new ArrayList<EgBirthDtl>();
        importJSon.setBirthCerts(birthCerts);
        DataFormatter dataFormatter = new DataFormatter();
        String[] columns= {"Registration No","Hospital Name","Reporting Date","Date of Birth","Sex","First name","Middle name","Last name","Place of birth (House / Hospital / Others)",
        		"First name","Middle name","Last name","Aadhaar Number","Email Id","Mobile No ","Education Qualification","Profession ","Nationality ","Religion ",
        		"First name","Middle name","Last name","Aadhaar Number","Email Id","Mobile No","Education Qualification","Profession ","Nationality ","Religion ",
        		"Bldg.No & Name","House No.","Street / Lane Name","Locality/Post Office","Tehsil","District","City Name","State","Pin Number","Country (India/Nepal/Others)",
        		"Bldg.No & Name","House No","Street / Lane Name","Locality/Post Office","Tehsil","District","City Name","State","Pin Number","Country (India/Nepal/Others)","Name","Address","Remarks"};
        try
        {
            if(files == null || tenantId==null )
                throw new CustomException("EG_FILESTORE_INVALID_INPUT","Invalid input provided");   
            for(MultipartFile file : files) {
				
				extension = FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase();
				if (!extension.equalsIgnoreCase("xlsx")) {
					throw new CustomException("EG_FILESTORE_INVALID_INPUT", "Inalvid input provided for file : "
							+ extension + ", please upload any of the allowed formats : xlsx ");
				}
				 
                String filename = FilenameUtils.getBaseName(file.getOriginalFilename()).toLowerCase();
                testFile = Files.createTempFile(filename, ".xlsx");
                file.transferTo(testFile.toFile());
                try(Workbook workbook = WorkbookFactory.create(testFile.toFile())){
                    Sheet sheet = workbook.getSheetAt(0);
                    Iterator<Row> rowIterator = sheet.rowIterator();
                    while(rowIterator.hasNext())
                    {
                    	Row row = rowIterator.next();
                    	if (row.getRowNum()>=4)
                    	{
                    		break;
                    	}
                    }
                    while (rowIterator.hasNext()) {
                        Row row = rowIterator.next();
                        Iterator<Cell> cellIterator = row.cellIterator();
                        EgBirthDtl birthDtl =  new EgBirthDtl();
                        birthDtl.setTenantid(tenantId);
                    	birthDtl.setCounter(0);
                    	birthDtl.setExcelrowindex(""+(row.getRowNum()+1));
                    	birthCerts.add(birthDtl);
                    	
                    	EgBirthFatherInfo birthFatherInfo = new EgBirthFatherInfo();
                    	EgBirthMotherInfo birthMotherInfo = new EgBirthMotherInfo();
                    	EgBirthPermaddr birthPermaddr = new EgBirthPermaddr();
                    	EgBirthPresentaddr birthPresentaddr = new EgBirthPresentaddr();
                    	birthDtl.setBirthFatherInfo(birthFatherInfo);
                    	birthDtl.setBirthMotherInfo(birthMotherInfo);
                    	birthDtl.setBirthPermaddr(birthPermaddr);
                    	birthDtl.setBirthPresentaddr(birthPresentaddr);
                    	while (cellIterator.hasNext()) {
                    		Cell cell = cellIterator.next();
                    			
                    		switch(cell.getColumnIndex()+1)
                    		{
                    		case 2:
                    			birthDtl.setRegistrationno(getStringVal(cell));
                    			break;
                    		case 3:
                    			birthDtl.setHospitalname(getStringVal(cell));
                    			break;
                    		case 4:
                    			birthDtl.setDateofreportepoch(String.valueOf(cell.getCellType()==CellType.NUMERIC?cell.getDateCellValue().getTime()/1000l:cell.getStringCellValue()));
                    			break;                            	
                    		case 5:
                    			birthDtl.setDateofbirthepoch(String.valueOf(cell.getCellType()==CellType.NUMERIC?cell.getDateCellValue().getTime()/1000l:cell.getStringCellValue()));
                    			break;
                    		case 6:
                    			birthDtl.setGenderStr(getStringVal(cell));
                    			break;
                    		case 7:
                    			birthDtl.setFirstname(getStringVal(cell));
                    			break;
                    		case 8:
                    			birthDtl.setMiddlename(getStringVal(cell));
                    			break;	
                    		case 9:
                    			birthDtl.setLastname(getStringVal(cell));
                    			break;
                    		case 10:
                    			birthDtl.setPlaceofbirth(getStringVal(cell));
                    			break;	
                    		case 11:
                    			birthFatherInfo.setFirstname(getStringVal(cell));
                    			break;
                    		case 12:
                    			birthFatherInfo.setMiddlename(getStringVal(cell));
                    			break;
                    		case 13:
                    			birthFatherInfo.setLastname(getStringVal(cell));
                    			break;
                    		case 14:
                    			birthFatherInfo.setAadharno(getStringVal(cell));
                    			break;
                    		case 15:
                    			birthFatherInfo.setEmailid(getStringVal(cell));
                    			break;
                    		case 16:
                    			birthFatherInfo.setMobileno(getStringVal(cell));
                    			break; 
                    		case 17:
                    			birthFatherInfo.setEducation(getStringVal(cell));
                    			break;
                    		case 18:
                    			birthFatherInfo.setProffession(getStringVal(cell));
                    			break;
                    		case 19:
                    			birthFatherInfo.setNationality(getStringVal(cell));
                    			break;
                    		case 20:
                    			birthFatherInfo.setReligion(getStringVal(cell));
                    			break; 
                    		case 21:
                    			birthMotherInfo.setFirstname(getStringVal(cell));
                    			break;
                    		case 22:
                    			birthMotherInfo.setMiddlename(getStringVal(cell));
                    			break;
                    		case 23:
                    			birthMotherInfo.setLastname(getStringVal(cell));
                    			break;
                    		case 24:
                    			birthMotherInfo.setAadharno(getStringVal(cell));
                    			break;
                    		case 25:
                    			birthMotherInfo.setEmailid(getStringVal(cell));
                    			break;
                    		case 26:
                    			birthMotherInfo.setMobileno(getStringVal(cell));
                    			break; 
                    		case 27:
                    			birthMotherInfo.setEducation(getStringVal(cell));
                    			break;
                    		case 28:
                    			birthMotherInfo.setProffession(getStringVal(cell));
                    			break;
                    		case 29:
                    			birthMotherInfo.setNationality(getStringVal(cell));
                    			break;
                    		case 30:
                    			birthMotherInfo.setReligion(getStringVal(cell));
                    			break;    
                    		case 31:
                    			birthPermaddr.setBuildingno(getStringVal(cell));
                    			break;  
                    		case 32:
                    			birthPermaddr.setHouseno(getStringVal(cell));
                    			break;
                    		case 33:
                    			birthPermaddr.setStreetname(getStringVal(cell));
                    			break;  
                    		case 34:
                    			birthPermaddr.setLocality(getStringVal(cell));
                    			break;  
                    		case 35:
                    			birthPermaddr.setTehsil(getStringVal(cell));
                    			break;  
                    		case 36:
                    			birthPermaddr.setDistrict(getStringVal(cell));
                    			break;  
                    		case 37:
                    			birthPermaddr.setCity(getStringVal(cell));
                    			break; 
                    		case 38:
                    			birthPermaddr.setState(getStringVal(cell));
                    			break;    
                    		case 39:
                    			birthPermaddr.setPinno(getStringVal(cell));
                    			break; 
                    		case 40:
                    			birthPermaddr.setCountry(getStringVal(cell));
                    			break;   
                    		case 41:
                    			birthPresentaddr.setBuildingno(getStringVal(cell));
                    			break;  
                    		case 42:
                    			birthPresentaddr.setHouseno(getStringVal(cell));
                    			break;
                    		case 43:
                    			birthPresentaddr.setStreetname(getStringVal(cell));
                    			break;  
                    		case 44:
                    			birthPresentaddr.setLocality(getStringVal(cell));
                    			break;  
                    		case 45:
                    			birthPresentaddr.setTehsil(getStringVal(cell));
                    			break;  
                    		case 46:
                    			birthPresentaddr.setDistrict(getStringVal(cell));
                    			break;  
                    		case 47:
                    			birthPresentaddr.setCity(getStringVal(cell));
                    			break; 
                    		case 48:
                    			birthPresentaddr.setState(getStringVal(cell));
                    			break;    
                    		case 49:
                    			birthPresentaddr.setPinno(getStringVal(cell));
                    			break; 
                    		case 50:
                    			birthPresentaddr.setCountry(getStringVal(cell));
                    			break;
                    		case 51:
                    			birthDtl.setInformantsname(getStringVal(cell));
                    			break; 
                    		case 52:
                    			birthDtl.setInformantsaddress(getStringVal(cell));
                    			break;   
                    		case 53:
                    			birthDtl.setRemarks(getStringVal(cell));
                    			break;                              	
                    		}

                    		//System.out.print(getStringVal(cell) + "-" + cell.getColumnIndex());
                    	}
                    	//System.out.println("Imported Row No "+row.getRowNum());
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                 
				RequestInfo requestInfo = new RequestInfo();
				User userInfo = new User();
				userInfo.setUuid("import-user");
				requestInfo.setUserInfo(userInfo);
				ImportBirthWrapper importBirthWrapper = commonService.saveBirthImport(importJSon,requestInfo);
                importBirthWrapper.setResponseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(requestInfo, true));
                return new ResponseEntity<>(importBirthWrapper, HttpStatus.OK);
            }
        }catch (Exception e) {
        	e.printStackTrace();
            throw new CustomException("EG_FILESTORE_INVALID_INPUT","Inalvid input provided for file : " + extension + ", please upload any of the allowed formats : xlsx ");
        }
        finally {
            try {
                Files.delete(testFile);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
		}
		return null;
    }
	
	
	@RequestMapping(value = { "/_death"}, method = RequestMethod.POST)
    @PostMapping(produces = APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<ImportDeathWrapper> uploadDeath(@RequestParam(value = "file", required = false) List<MultipartFile> files,
            @RequestParam(value = "tenantId" , required = false) String tenantId) {
		if(excelImportFlag)	{
        String extension = "";
        Path testFile = null;
        DeathResponse importJSon = new DeathResponse();
        List<EgDeathDtl> deathCerts = new ArrayList<EgDeathDtl>();
        importJSon.setDeathCerts(deathCerts);
        DataFormatter dataFormatter = new DataFormatter();
        String[] columns= {"Registration No","Hospital name","Registration Date","Date Of Death","Sex ","Age","First name","Middle name","Last name","EID No","Aadhaar No",
        		"Nationality ","Religion ","Place of Death(House/Hospital/other)","ICD Code",
        		"First name","Middle name","Last name","Aadhaar Number","Email Id","Mobile No",
        		"First name","Middle name","Last name","Aadhaar Number","Email Id","Mobile No",
        		"First name","Middle name","Last name","Aadhaar Number","Email Id","Mobile No",
        		"Bldg.No & Name","House No","Street / Lane Name","Locality/Post Office","Tehsil","District","State","City Name","Pin Number","Country (India/Nepal/Others)",
        		"Bldg.No & Name","House No","Street / Lane Name","Locality/Post Office","Tehsil","District","State","City Name","Pin Number","Country (India/Nepal/Others)","Name","Address","Remarks"};
        try
        {
            if(files == null || tenantId==null )
                throw new CustomException("EG_FILESTORE_INVALID_INPUT","Invalid input provided");   
            for(MultipartFile file : files) {
				
				extension = FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase();
				if (!extension.equalsIgnoreCase("xlsx")) {
					throw new CustomException("EG_FILESTORE_INVALID_INPUT", "Inalvid input provided for file : "
							+ extension + ", please upload any of the allowed formats : xlsx ");
				}
				 
                String filename = FilenameUtils.getBaseName(file.getOriginalFilename()).toLowerCase();
                testFile = Files.createTempFile(filename, ".xlsx");
                file.transferTo(testFile.toFile());
                try(Workbook workbook = WorkbookFactory.create(testFile.toFile())){
                    Sheet sheet = workbook.getSheetAt(0);
                    Iterator<Row> rowIterator = sheet.rowIterator();
                    
                    while(rowIterator.hasNext())
                    {
                    	Row row = rowIterator.next();
                    	if (row.getRowNum()>=4)
                    	{
                    		break;
                    	}
                    }
                    while (rowIterator.hasNext()) {
                        Row row = rowIterator.next();
                        Iterator<Cell> cellIterator = row.cellIterator();
                        EgDeathDtl deathDtl =  new EgDeathDtl();
                        deathDtl.setTenantid(tenantId);
                    	deathDtl.setCounter(0);
                    	deathDtl.setExcelrowindex(""+(row.getRowNum()+1));
                    	deathCerts.add(deathDtl);
                    	
                    	EgDeathFatherInfo deathFatherInfo = new EgDeathFatherInfo();
                    	EgDeathMotherInfo deathMotherInfo = new EgDeathMotherInfo();
                    	EgDeathPermaddr deathPermaddr = new EgDeathPermaddr();
                    	EgDeathPresentaddr deathPresentaddr = new EgDeathPresentaddr();
                    	EgDeathSpouseInfo deathSpouseInfo = new EgDeathSpouseInfo();
                    	deathDtl.setDeathFatherInfo(deathFatherInfo);
                    	deathDtl.setDeathMotherInfo(deathMotherInfo);
                    	deathDtl.setDeathSpouseInfo(deathSpouseInfo);
                    	deathDtl.setDeathPermaddr(deathPermaddr);
                    	deathDtl.setDeathPresentaddr(deathPresentaddr);
                    	while (cellIterator.hasNext()) {
                    		Cell cell = cellIterator.next();
                    			
                    		switch(cell.getColumnIndex()+1)
                    		{
                    		case 2:
                    			deathDtl.setRegistrationno(getStringVal(cell));
                    			break;
                    		case 3:
                    			deathDtl.setHospitalname(getStringVal(cell));
                    			break;
                    		case 4:
                    			deathDtl.setDateofreportepoch(String.valueOf(cell.getCellType()==CellType.NUMERIC?cell.getDateCellValue().getTime()/1000l:cell.getStringCellValue()));
                    			break;                            	
                    		case 5:
                    			deathDtl.setDateofdeathepoch(String.valueOf(cell.getCellType()==CellType.NUMERIC?cell.getDateCellValue().getTime()/1000l:cell.getStringCellValue()));
                    			break;
                    		case 6:
                    			deathDtl.setGenderStr(getStringVal(cell));
                    			break;
                    		case 7:
                    			deathDtl.setAge(getStringVal(cell));
                    			break;
                    		case 8:
                    			deathDtl.setFirstname(getStringVal(cell));
                    			break;
                    		case 9:
                    			deathDtl.setMiddlename(getStringVal(cell));
                    			break;	
                    		case 10:
                    			deathDtl.setLastname(getStringVal(cell));
                    			break;
                    		case 11:
                    			deathDtl.setEidno(getStringVal(cell));
                    			break;
                    		case 12:
                    			deathDtl.setAadharno(getStringVal(cell));
                    			break;
                    		case 13:
                    			deathDtl.setNationality(getStringVal(cell));
                    			break;
                    		case 14:
                    			deathDtl.setReligion(getStringVal(cell));
                    			break;
                    		case 15:
                    			deathDtl.setPlaceofdeath(getStringVal(cell));
                    			break;	
                    		case 16:
                    			deathDtl.setIcdcode(getStringVal(cell));
                    			break;
                    		case 17:
                    			deathSpouseInfo.setFirstname(getStringVal(cell));
                    			break;
                    		case 18:
                    			deathSpouseInfo.setMiddlename(getStringVal(cell));
                    			break;
                    		case 19:
                    			deathSpouseInfo.setLastname(getStringVal(cell));
                    			break;
                    		case 20:
                    			deathSpouseInfo.setAadharno(getStringVal(cell));
                    			break;
                    		case 21:
                    			deathSpouseInfo.setEmailid(getStringVal(cell));
                    			break;
                    		case 22:
                    			deathSpouseInfo.setMobileno(getStringVal(cell));
                    			break; 
                    		case 23:
                    			deathFatherInfo.setFirstname(getStringVal(cell));
                    			break;
                    		case 24:
                    			deathFatherInfo.setMiddlename(getStringVal(cell));
                    			break;
                    		case 25:
                    			deathFatherInfo.setLastname(getStringVal(cell));
                    			break;
                    		case 26:
                    			deathFatherInfo.setAadharno(getStringVal(cell));
                    			break;
                    		case 27:
                    			deathFatherInfo.setEmailid(getStringVal(cell));
                    			break;
                    		case 28:
                    			deathFatherInfo.setMobileno(getStringVal(cell));
                    			break; 
                    		case 29:
                    			deathMotherInfo.setFirstname(getStringVal(cell));
                    			break;
                    		case 30:
                    			deathMotherInfo.setMiddlename(getStringVal(cell));
                    			break;
                    		case 31:
                    			deathMotherInfo.setLastname(getStringVal(cell));
                    			break;
                    		case 32:
                    			deathMotherInfo.setAadharno(getStringVal(cell));
                    			break;
                    		case 33:
                    			deathMotherInfo.setEmailid(getStringVal(cell));
                    			break;
                    		case 34:
                    			deathMotherInfo.setMobileno(getStringVal(cell));
                    			break; 
                    		case 35:
                    			deathPermaddr.setBuildingno(getStringVal(cell));
                    			break;  
                    		case 36:
                    			deathPermaddr.setHouseno(getStringVal(cell));
                    			break;
                    		case 37:
                    			deathPermaddr.setStreetname(getStringVal(cell));
                    			break;  
                    		case 38:
                    			deathPermaddr.setLocality(getStringVal(cell));
                    			break;  
                    		case 39:
                    			deathPermaddr.setTehsil(getStringVal(cell));
                    			break;  
                    		case 40:
                    			deathPermaddr.setDistrict(getStringVal(cell));
                    			break;  
                    		case 41:
                    			deathPermaddr.setState(getStringVal(cell));
                    			break;
                    		case 42:
                    			deathPermaddr.setCity(getStringVal(cell));
                    			break; 
                    		case 43:
                    			deathPermaddr.setPinno(getStringVal(cell));
                    			break; 
                    		case 44:
                    			deathPermaddr.setCountry(getStringVal(cell));
                    			break;   
                    		case 45:
                    			deathPresentaddr.setBuildingno(getStringVal(cell));
                    			break;  
                    		case 46:
                    			deathPresentaddr.setHouseno(getStringVal(cell));
                    			break;
                    		case 47:
                    			deathPresentaddr.setStreetname(getStringVal(cell));
                    			break;  
                    		case 48:
                    			deathPresentaddr.setLocality(getStringVal(cell));
                    			break;  
                    		case 49:
                    			deathPresentaddr.setTehsil(getStringVal(cell));
                    			break;  
                    		case 50:
                    			deathPresentaddr.setDistrict(getStringVal(cell));
                    			break;  
                    		case 51:
                    			deathPresentaddr.setState(getStringVal(cell));
                    			break;
                    		case 52:
                    			deathPresentaddr.setCity(getStringVal(cell));
                    			break; 
                    		case 53:
                    			deathPresentaddr.setPinno(getStringVal(cell));
                    			break; 
                    		case 54:
                    			deathPresentaddr.setCountry(getStringVal(cell));
                    			break;
                    		case 55:
                    			deathDtl.setInformantsname(getStringVal(cell));
                    			break; 
                    		case 56:
                    			deathDtl.setInformantsaddress(getStringVal(cell));
                    			break;   
                    		case 57:
                    			deathDtl.setRemarks(getStringVal(cell));
                    			break;                              	
                    		}

                    		//System.out.print(getStringVal(cell) + "-" + cell.getColumnIndex());
                    	}
                    	//System.out.println("Imported Row No "+row.getRowNum());
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                RequestInfo requestInfo = new RequestInfo();
				User userInfo = new User();
				userInfo.setUuid("import-user");
				requestInfo.setUserInfo(userInfo);
				ImportDeathWrapper importDeathWrapper = commonService.saveDeathImport(importJSon,requestInfo);
                importDeathWrapper.setResponseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(requestInfo, true));
                return new ResponseEntity<>(importDeathWrapper, HttpStatus.OK);
            }
        }catch (Exception e) {
        	e.printStackTrace();
            throw new CustomException("EG_FILESTORE_INVALID_INPUT","Inalvid input provided for file : " + extension + ", please upload any of the allowed formats : xlsx ");
        }
        finally {
            try {
                Files.delete(testFile);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
		}
		return null;
    }
    
    private String getStringVal(Cell cell)
    {
    	return cell.getCellType() == CellType.NUMERIC ? String.valueOf((long)cell.getNumericCellValue()) : cell.getStringCellValue();
    }
}
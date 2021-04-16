
package org.egov.pt.excelimport;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.egov.pt.models.Assessment;
import org.egov.pt.service.AssessmentService;
import org.egov.pt.service.PropertyService;
import org.egov.pt.util.ResponseInfoFactory;
import org.egov.pt.web.contracts.AssessmentRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.databind.JsonNode;



@Controller
@RequestMapping("/excelimport")
public class ImportController {

	@Autowired
	private AssessmentService assessmentService;
	
	@Autowired
	private ResponseInfoFactory responseInfoFactory;
	
	@Autowired
	private PropertyService propertyService;
	
    @RequestMapping(value = "/_import", method = RequestMethod.POST)
	public ResponseEntity<String> _import() {

    	ArrayList<String> tenantIds = new ArrayList<String>(
    			Arrays.asList("pb.testing","pb.agra","pb.ahmedabad","pb.ahmednagar","pb.ajmer","pb.allahabad","pb.almora","pb.ambala","pb.amritsar","pb.aurangabad","pb.babina","pb.badamibagh"
    			,"pb.bakloh","pb.bareilly","pb.barrackpore","pb.belgaum","pb.cannanore","pb.chakrata","pb.clementtown","pb.dagshai","pb.dalhousie","pb.danapur","pb.dehradun","pb.dehuroad",
    			"pb.delhi","pb.deolali","pb.faizabad","pb.fatehgarh","pb.ferozepur","pb.jabalpur","pb.jalandhar","pb.jalapahar","pb.jammu","pb.jhansi","pb.jutogh","pb.kamptee","pb.kanpur",
    			"pb.kasauli","pb.khasyol","pb.kirkee","pb.landour","pb.lansdowne","pb.lebong","pb.lucknow","pb.mathura","pb.meerut","pb.mhow","pb.morar","pb.nainital","pb.nasirabad",
    			"pb.pachmarhi","pb.pune","pb.ramgarh","pb.ranikhet","pb.roorkee","pb.saugor","pb.secunderabad","pb.shahjahanpur","pb.shillong","pb.stm","pb.subathu","pb.varanasi","pb.wellington"));
    	String rootDir = "D:\\project_docs\\PYTAX_DETAILS_Echhawani-20210416T045357Z-001\\PYTAX_DETAILS_Echhawani";
    	Path start = Paths.get(rootDir);
		try (Stream<Path> stream = Files.walk(start, 2)) {
			List<String> collect = stream
					.filter(Files::isRegularFile)
					.map(String::valueOf)
					.sorted()
					.collect(Collectors.toList());
			for (String filepath : collect) {
				String ext = FilenameUtils.getExtension(filepath);
				if (ext.equalsIgnoreCase("xlsx")) {
					System.out.println(filepath+" : "+ext);
					String tenantId = FilenameUtils.getBaseName(filepath).replace("CB_", "pb.").toLowerCase();
					if(tenantIds.contains(tenantId)) {
						int rownum=0;
		                try(Workbook workbook = WorkbookFactory.create(new File(filepath))){
		                    Sheet sheet = workbook.getSheetAt(0);
		                    Iterator<Row> rowIterator = sheet.rowIterator();
		                    while(rowIterator.hasNext())
		                    {
		                    	Row row = rowIterator.next();
		                    	if (row.getRowNum()>=0)
		                    	{
		                    		break;
		                    	}
		                    }
		                    while (rowIterator.hasNext()) {
		                        Row row = rowIterator.next();
		                        Assessment assessment = new Assessment();
		                        JsonNode additionalDetails = null;
		                        Iterator<Cell> cellIterator = row.cellIterator();
		                    	while (cellIterator.hasNext()) {
		                    		Cell cell = cellIterator.next();
		                    		try
		                    		{
		                    			switch(cell.getColumnIndex()+1)
		                    			{
		                    			case 1:
		                    				//System.out.print(getStringVal(cell));
		                    				/*Set<String> ids = new HashSet<String>();
		                    				ids.add(getStringVal(cell));
		                    				PropertyCriteria propertyCriteria = PropertyCriteria.builder().tenantId(tenantId).oldpropertyids(ids).build();
		                    				
											Property property = propertyService.searchProperty(propertyCriteria,null).get(0);
											assessment.setTenantId(tenantId);
											assessment.setPropertyId(property.getPropertyId());
											assessment.setFinancialYear("2021-22");
											assessment.setAssessmentDate(System.currentTimeMillis());
											assessment.setSource(Source.LEGACY_RECORD);
											assessment.setChannel(property.getChannel());
											assessment.setStatus(property.getStatus());*/
											
		                    				break;
		                    			case 2:
		                    				//System.out.print(getStringVal(cell));
		                    				// to be filled assessment.setAdditionalDetails(additionalDetails)
		                    				break;
		                    			case 3:
		                    				//System.out.println(getStringVal(cell));
		                    				//to be filled assessment.setAdditionalDetails(additionalDetails)
		                    				break;
		                    			}
		                    		}catch (Exception e) {
		                    			e.printStackTrace();
									}
		                    		//System.out.print(getStringVal(cell));
		                    	}
		                    	AssessmentRequest assessmentRequest = AssessmentRequest.builder().assessment(assessment).requestInfo(null).build();
								Assessment assessments = null;
								// to be called assessments = assessmentService.createAssessment(assessmentRequest);
								rownum++;
		                    	System.out.println("Imported Row No : "+(row.getRowNum()+1));
		                    }
		                }catch(Exception e){
		                	 e.printStackTrace();
		                	 throw new CustomException("INVALID_EXCEL","Excel is not valid");
		                }
		                if(rownum>0) {
			                File file = new File(rootDir,FilenameUtils.getBaseName(filepath)+".txt");
			                FileWriter myWriter = new FileWriter(file);
			                myWriter.write("done "+rownum);
			                myWriter.close();
		                }
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ResponseEntity<>("Success", HttpStatus.OK);
	}
    
    private String getStringVal(Cell cell)
    {
    	return cell.getCellType() == CellType.NUMERIC ? String.valueOf((long)cell.getNumericCellValue()) : cell.getStringCellValue();
    }
    
}
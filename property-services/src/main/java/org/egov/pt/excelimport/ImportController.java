
package org.egov.pt.excelimport;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import org.egov.pt.models.Assessment.Source;
import org.egov.pt.models.Demand;
import org.egov.pt.models.DemandDetail;
import org.egov.pt.models.Property;
import org.egov.pt.models.PropertyCriteria;
import org.egov.pt.models.User;
import org.egov.pt.service.AssessmentService;
import org.egov.pt.service.PropertyService;
import org.egov.pt.util.ResponseInfoFactory;
import org.egov.pt.web.contracts.AssessmentRequest;
import org.egov.pt.web.contracts.RequestInfoWrapper;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;

@Controller
@RequestMapping("/excelimport")
public class ImportController {

	@Autowired
	private AssessmentService assessmentService;

	@Autowired
	private ResponseInfoFactory responseInfoFactory;

	@Autowired
	private PropertyService propertyService;

	@Autowired
	@Qualifier("objectMapper")
	private ObjectMapper om;

	@RequestMapping(value = "/_import", method = RequestMethod.POST)
	public ResponseEntity<String> _import(@RequestBody RequestInfoWrapper requestInfoWrapper) {

		ArrayList<String> tenantIds = new ArrayList<String>(Arrays.asList("pb.testing", "pb.agra", "pb.ahmedabad",
				"pb.ahmednagar", "pb.ajmer", "pb.allahabad", "pb.almora", "pb.ambala", "pb.amritsar", "pb.aurangabad",
				"pb.babina", "pb.badamibagh", "pb.bakloh", "pb.bareilly", "pb.barrackpore", "pb.belgaum",
				"pb.cannanore", "pb.chakrata", "pb.clementtown", "pb.dagshai", "pb.dalhousie", "pb.danapur",
				"pb.dehradun", "pb.dehuroad", "pb.delhi", "pb.deolali", "pb.faizabad", "pb.fatehgarh", "pb.ferozepur",
				"pb.jabalpur", "pb.jalandhar", "pb.jalapahar", "pb.jammu", "pb.jhansi", "pb.jutogh", "pb.kamptee",
				"pb.kanpur", "pb.kasauli", "pb.khasyol", "pb.kirkee", "pb.landour", "pb.lansdowne", "pb.lebong",
				"pb.lucknow", "pb.mathura", "pb.meerut", "pb.mhow", "pb.morar", "pb.nainital", "pb.nasirabad",
				"pb.pachmarhi", "pb.pune", "pb.ramgarh", "pb.ranikhet", "pb.roorkee", "pb.saugor", "pb.secunderabad",
				"pb.shahjahanpur", "pb.shillong", "pb.stm", "pb.subathu", "pb.varanasi", "pb.wellington"));

		Map<String, String> taxHeadMaps = new HashMap<String, String>();
		taxHeadMaps.put("House Tax", "PT_HOUSE_TAX");
		taxHeadMaps.put("Water Tax", "PT_WATER_TAX");
		taxHeadMaps.put("Conservancy and Scavening Tax", "PT_CONSERVANCY_TAX");
		taxHeadMaps.put("Lighting And Drainage Tax", "PT_LIGHTINING_TAX");
		taxHeadMaps.put("Education Tax", "PT_EDUCATION_TAX");

		String rootDir = "D:\\project_docs\\PYTAX_DETAILS_Echhawani-20210416T045357Z-001\\PYTAX_DETAILS_Echhawani";
		Path start = Paths.get(rootDir);
		try (Stream<Path> stream = Files.walk(start, 2)) {
			List<String> collect = stream.filter(Files::isRegularFile).map(String::valueOf).sorted()
					.collect(Collectors.toList());
			Map<String, List<Entry<String, List<ExcelColumns>>>> successMap = new HashMap<String, List<Entry<String, List<ExcelColumns>>>>();
			Map<String, List<Entry<String, List<ExcelColumns>>>> propertyNotFoundMap = new HashMap<String, List<Entry<String, List<ExcelColumns>>>>();
			Map<String, List<Entry<String, List<ExcelColumns>>>> invalidTaxCodeMap = new HashMap<String, List<Entry<String, List<ExcelColumns>>>>();
			for (String filepath : collect) {
				String ext = FilenameUtils.getExtension(filepath);
				if (ext.equalsIgnoreCase("xlsx")) {
					System.out.println(filepath + " : " + ext);
					String tenantId = FilenameUtils.getBaseName(filepath).replace("CB_", "pb.").toLowerCase();
					if (tenantIds.contains(tenantId)) {
						Map<String, List<ExcelColumns>> excelmap = new HashMap<String, List<ExcelColumns>>();
						Map<String, List<ExcelColumns>> excelmapErrorTaxCode = new HashMap<String, List<ExcelColumns>>();
						try (Workbook workbook = WorkbookFactory.create(new File(filepath))) {
							Sheet sheet = workbook.getSheetAt(0);
							Iterator<Row> rowIterator = sheet.rowIterator();
							while (rowIterator.hasNext()) {
								Row row = rowIterator.next();
								if (row.getRowNum() >= 0) {
									break;
								}
							}

							while (rowIterator.hasNext()) {
								Row row = rowIterator.next();
								ExcelColumns columns = new ExcelColumns();
								String key = "";
								Iterator<Cell> cellIterator = row.cellIterator();
								while (cellIterator.hasNext()) {
									Cell cell = cellIterator.next();

									try {
										switch (cell.getColumnIndex() + 1) {
										case 1:
											key = getStringVal(cell);
											break;
										case 2:
											String taxheadcode = getStringVal(cell);
											if (taxHeadMaps.get(taxheadcode) != null)
												columns.setTaxHeadMasterCode(taxHeadMaps.get(taxheadcode));
											// System.out.print(getStringVal(cell));
											break;
										case 3:
											columns.setTaxAmount(new BigDecimal(getStringVal(cell)));
											// System.out.println(getStringVal(cell));
											break;
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
									// System.out.print(getStringVal(cell));
								}
								if (null != columns.getTaxHeadMasterCode()
										&& !columns.getTaxHeadMasterCode().isEmpty()) {
									if (null == excelmap.get(key))
										excelmap.put(key, new ArrayList<ExcelColumns>());
									excelmap.get(key).add(columns);
								} else {
									excelmapErrorTaxCode.put(key, new ArrayList<ExcelColumns>());
								}
							}
							System.out.println("size : " + excelmap.size());
							System.out.println();
						} catch (Exception e) {
							e.printStackTrace();
							throw new CustomException("INVALID_EXCEL", "Excel data is not valid");
						}
						if (excelmap.entrySet().size() > 0) {
							/*
							 * File file = new File(rootDir, FilenameUtils.getBaseName(filepath) + ".txt");
							 * FileWriter myWriter = new FileWriter(file); myWriter.write(new
							 * Gson().toJson(excelmap)); myWriter.close();
							 */

							for (Entry<String, List<ExcelColumns>> entry : excelmap.entrySet()) {

								Set<String> ids = new HashSet<String>();
								ids.add(entry.getKey());
								PropertyCriteria propertyCriteria = PropertyCriteria.builder().tenantId(tenantId)
										.oldpropertyids(ids).build();
								if (propertyService
										.searchProperty(propertyCriteria, requestInfoWrapper.getRequestInfo())
										.size() > 0) {
									Property property = propertyService
											.searchProperty(propertyCriteria, requestInfoWrapper.getRequestInfo())
											.get(0);
									Assessment assessment = new Assessment();
									JsonNode additionalDetails = om.createObjectNode();

									assessment.setTenantId(tenantId);
									assessment.setPropertyId(property.getPropertyId());
									assessment.setFinancialYear("2021-22");
									assessment.setAssessmentDate(System.currentTimeMillis());
									assessment.setSource(Source.LEGACY_RECORD);
									assessment.setChannel(property.getChannel());
									assessment.setStatus(property.getStatus());

									((ObjectNode) additionalDetails).put("RequestInfo", "tt: tt");
									Demand demand = new Demand();
									demand.setTenantId(tenantId);
									demand.setConsumerCode(property.getPropertyId());
									demand.setConsumerType(property.getPropertyType());
									demand.setBusinessService("PT");
									demand.setTaxPeriodFrom(1617215400000l);// to be verified
									demand.setTaxPeriodTo(1648751399000l);// to be verified
									User payer = new User();
									payer.setUuid(property.getOwners().get(0).getUuid());// to be verified
									demand.setPayer(payer);
									List<DemandDetail> demandDetails = new ArrayList<DemandDetail>();
									for (ExcelColumns column : entry.getValue()) {
										DemandDetail detail = new DemandDetail();
										detail.setTaxHeadMasterCode(column.getTaxHeadMasterCode());
										detail.setTaxAmount(column.getTaxAmount());
										demandDetails.add(detail);
									}
									demand.setDemandDetails(demandDetails);
									// ObjectNode demandNode = om.valueToTree(demands);
									((ObjectNode) additionalDetails).putArray("Demands")
											.add(om.valueToTree(Arrays.asList(demand)));
									assessment.setAdditionalDetails(additionalDetails);

									/*
									 * JsonObject jsonObject = new JsonObject();
									 * jsonObject.addProperty("RequestInfo", "tt: tt"); JsonArray jsonArray = new
									 * JsonArray(); jsonArray.add(new Gson().toJson(demand));
									 * jsonObject.add("Demands", jsonArray);
									 * assessment.setAdditionalDetails(om.readTree(jsonObject.toString()));
									 */

									AssessmentRequest assessmentRequest = AssessmentRequest.builder()
											.assessment(assessment).requestInfo(requestInfoWrapper.getRequestInfo())
											.build();
									// Assessment assessments = null;
									// assessments = assessmentService.createLegacyAssessments(assessmentRequest);
									Path path = Paths.get(rootDir, FilenameUtils.getBaseName(filepath) + ".txt");
									Files.write(path,
											Arrays.asList("Success : Data for creating " + entry.getKey() + "\n "
													+ new Gson().toJson(assessment)),
											StandardCharsets.UTF_8,
											Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
									if (null == successMap.get(tenantId))
										successMap.put(tenantId, new ArrayList<Entry<String, List<ExcelColumns>>>());
									successMap.get(tenantId).add(entry);
								} else {
									Path path = Paths.get(rootDir, FilenameUtils.getBaseName(filepath) + ".txt");
									Files.write(path,
											Arrays.asList("Failure : Property not found for " + entry.getKey()),
											StandardCharsets.UTF_8,
											Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
									if (null == propertyNotFoundMap.get(tenantId))
										propertyNotFoundMap.put(tenantId,
												new ArrayList<Entry<String, List<ExcelColumns>>>());
									propertyNotFoundMap.get(tenantId).add(entry);
								}
							}
						} else if (excelmapErrorTaxCode.entrySet().size() > 0) {
							Path path = Paths.get(rootDir, FilenameUtils.getBaseName(filepath) + ".txt");
							Files.write(path, Arrays.asList("Invalid Tax code "), StandardCharsets.UTF_8,
									Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
							excelmapErrorTaxCode.entrySet().forEach(entry -> {
								try {
									Files.write(path, Arrays.asList(entry.getKey()), StandardCharsets.UTF_8,
											StandardOpenOption.APPEND);
									if (null == invalidTaxCodeMap.get(tenantId))
										invalidTaxCodeMap.put(tenantId,
												new ArrayList<Entry<String, List<ExcelColumns>>>());
									invalidTaxCodeMap.get(tenantId).add(entry);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							});

						} else {
							Path path = Paths.get(rootDir, FilenameUtils.getBaseName(filepath) + ".txt");
							Files.write(path, Arrays.asList("Error : No records to process"), StandardCharsets.UTF_8,
									Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
						}
					} else {
						Path path = Paths.get(rootDir, "_Invalid_Tenantid.txt");
						Files.write(path,
								Arrays.asList("Error : Invalid file name against tenantid : "
										+ FilenameUtils.getBaseName(filepath)),
								StandardCharsets.UTF_8,
								Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
					}
				} else {
					Path path = Paths.get(rootDir, "_Invalid_Excel.txt");
					Files.write(path,
							Arrays.asList("Error : Invalid file extension : " + FilenameUtils.getBaseName(filepath)),
							StandardCharsets.UTF_8,
							Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
				}
			}
			Path path = Paths.get(rootDir, "_Final_Summary.txt");
			Files.write(path, Arrays.asList("Success Report : "), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
			successMap.entrySet().forEach(success -> {
				try {
					Files.write(path, Arrays.asList(success.getKey() + " : " + success.getValue().size()),
							StandardCharsets.UTF_8, StandardOpenOption.APPEND);
					for (Entry<String, List<ExcelColumns>> values : success.getValue()) {
						try {
							Files.write(path, Arrays.asList(values.getKey()), StandardCharsets.UTF_8,
									StandardOpenOption.APPEND);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

			Files.write(path, Arrays.asList("Property Not Found Report : "), StandardCharsets.UTF_8,
					StandardOpenOption.APPEND);
			propertyNotFoundMap.entrySet().forEach(property -> {
				try {
					Files.write(path, Arrays.asList(property.getKey() + " : " + property.getValue().size()),
							StandardCharsets.UTF_8, StandardOpenOption.APPEND);
					for (Entry<String, List<ExcelColumns>> values : property.getValue()) {
						try {
							Files.write(path, Arrays.asList(values.getKey()), StandardCharsets.UTF_8,
									StandardOpenOption.APPEND);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

			Files.write(path, Arrays.asList("Invalid Tax code Report : "), StandardCharsets.UTF_8,
					StandardOpenOption.APPEND);
			invalidTaxCodeMap.entrySet().forEach(taxcode -> {
				try {
					Files.write(path, Arrays.asList(taxcode.getKey() + " : " + taxcode.getValue().size()),
							StandardCharsets.UTF_8, StandardOpenOption.APPEND);
					for (Entry<String, List<ExcelColumns>> values : taxcode.getValue()) {
						try {
							Files.write(path, Arrays.asList(values.getKey()), StandardCharsets.UTF_8,
									StandardOpenOption.APPEND);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ResponseEntity<>("Failed", HttpStatus.OK);
		}
		return new ResponseEntity<>("Success", HttpStatus.OK);
	}

	private String getStringVal(Cell cell) {
		return cell.getCellType() == CellType.NUMERIC ? String.valueOf((long) cell.getNumericCellValue())
				: cell.getStringCellValue();
	}
}
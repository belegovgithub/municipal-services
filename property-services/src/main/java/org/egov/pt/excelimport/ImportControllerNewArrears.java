
package org.egov.pt.excelimport;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
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

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.models.Assessment;
import org.egov.pt.models.Assessment.Source;
import org.egov.pt.models.AssessmentSearchCriteria;
import org.egov.pt.models.Demand;
import org.egov.pt.models.DemandDetail;
import org.egov.pt.models.Property;
import org.egov.pt.models.PropertyCriteria;
import org.egov.pt.models.User;
import org.egov.pt.repository.ServiceRequestRepository;
import org.egov.pt.service.AssessmentService;
import org.egov.pt.service.PropertyService;
import org.egov.pt.util.ResponseInfoFactory;
import org.egov.pt.web.contracts.AssessmentRequest;
import org.egov.pt.web.contracts.DemandRequest;
import org.egov.pt.web.contracts.DemandResponse;
import org.egov.pt.web.contracts.RequestInfoWrapper;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/excelimportnewarrear")
public class ImportControllerNewArrears {

	@Autowired
	private AssessmentService assessmentService;

	@Autowired
	private ResponseInfoFactory responseInfoFactory;

	@Autowired
	private PropertyService propertyService;

	@Autowired
	private PropertyConfiguration config;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;
	
	@Value("${egov.ptdemand.excelimport.tokenflag}")
    private boolean excelImportTokenFlag;
	
	@Value("${egov.ptdemand.excelimport.token}")
    private String excelImportToken;

	@RequestMapping(value = { "/_import" }, method = RequestMethod.POST)
	@PostMapping(produces = APPLICATION_JSON_UTF8_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public ResponseEntity<ImportReportWrapper> _import(@RequestParam(value = "file", required = false) List<MultipartFile> files,
			@RequestParam(value = "tenantId", required = false) String tenantId ,
			@RequestParam(value = "RequestInfo", required = false) String req, 
			@RequestParam(value = "token", required = false) String token) {
		if(excelImportTokenFlag ? excelImportToken.equals(token) : true)	{

		RequestInfo requestInfo = null;
		try {
			requestInfo = mapper.readValue(req, RequestInfo.class);
		} catch (JsonMappingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Map<String, String> taxHeadMaps = new HashMap<String, String>();
		taxHeadMaps.put("House Tax", "PT_HOUSE_TAX");
		taxHeadMaps.put("Water Tax", "PT_WATER_TAX");
		taxHeadMaps.put("Conservancy and Scavening Tax", "PT_CONSERVANCY_TAX");
		taxHeadMaps.put("Lighting And Drainage Tax", "PT_LIGHTINING_TAX");
		taxHeadMaps.put("Education Tax", "PT_EDUCATION_TAX");
		taxHeadMaps.put("Consolidated Property Tax", "PT_CONSOLIDATED_PROPERTY_TAX");
		taxHeadMaps.put("Sanitary Cess", "PT_SANITARY_CESS");
		taxHeadMaps.put("Education Cess", "PT_EDUCATION_CESS");
		taxHeadMaps.put("Additional water tax", "PT_ADDL_WATER_TAX");
		taxHeadMaps.put("Drainage Tax", "PT_DRAINAGE_TAX");
		taxHeadMaps.put("Lighting Tax", "PT_LIGHTING_TAX");
		taxHeadMaps.put("Advance", "PT_ADVANCE_CARRYFORWARD");
		taxHeadMaps.put("Interest", "PT_TIME_INTEREST");
		taxHeadMaps.put("Demand Notice Charge", "PT_DEMANDNOTICE_CHARGE");
		taxHeadMaps.put("Scavenging", "PT_SCAVENGING_TAX");
		
		ImportReportWrapper wrapper = new ImportReportWrapper();
		String extension = "";
		Path testFile = null;
		try {
			if (files == null || tenantId == null)
				throw new CustomException("EG_FILESTORE_INVALID_INPUT", "Invalid input provided");
			for (MultipartFile file : files) {

				extension = FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase();
				if (!extension.equalsIgnoreCase("xlsx")) {
					throw new CustomException("EG_FILESTORE_INVALID_INPUT", "Inalvid input provided for file : "
							+ extension + ", please upload any of the allowed formats : xlsx ");
				}

				String filename = FilenameUtils.getBaseName(file.getOriginalFilename()).toLowerCase();
				testFile = Files.createTempFile(filename, ".xlsx");
				file.transferTo(testFile.toFile());
				Map<String, List<ExcelColumns>> excelmap = new HashMap<String, List<ExcelColumns>>();
				try (Workbook workbook = WorkbookFactory.create(testFile.toFile())) {
					Sheet sheet = workbook.getSheetAt(0);
					Iterator<Row> rowIterator = sheet.rowIterator();
					Row firstRow = sheet.getRow(0);
					FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
					List<String> duplicatePropertyIds = new ArrayList<>();
					Map<Integer,String> taxheads = new HashMap<>();
					for (int i = 1; i < firstRow.getPhysicalNumberOfCells(); i++) {
						Cell header_cell = firstRow.getCell(i);
						String header =  header_cell.getStringCellValue();
						if (taxHeadMaps.get(header) != null)
							taxheads.put(i, taxHeadMaps.get(header));
						else {
							wrapper.updateMaps(ImportReportWrapper.taxCodeNotFoundForTaxHeadReport, header);
						}

					}
					System.out.println("taxheads::"+taxheads);
					while (rowIterator.hasNext()) {
						Row row = rowIterator.next();
						if (row.getRowNum() >= 0) {
							break;
						}
					}

					while (rowIterator.hasNext()) {
						Row row = rowIterator.next();
						String key = "";
						Iterator<Cell> cellIterator = row.cellIterator();
						while (cellIterator.hasNext()) {
							ExcelColumns columns = new ExcelColumns();

							Cell cell = cellIterator.next();
							CellValue cellValue = evaluator.evaluate(cell);
							Integer index = cell.getColumnIndex();

							try {
								if(index==0) {
									if(tenantId.equalsIgnoreCase("pb.jalandhar"))
										key = String.valueOf((int) cellValue.getNumberValue());								
									else
										key = getStringVal(cellValue);
									if(excelmap.get(key)!=null || duplicatePropertyIds.contains(key))
									{
										duplicatePropertyIds.add(key);
										wrapper.updateMaps(ImportReportWrapper.duplicateRecords, key);
										excelmap.remove(key);
										break;
									}
								}
								else {
									System.out.println(taxheads.get(index));
									if (taxheads.get(index) != null) {
										columns.setTaxHeadMasterCode(taxheads.get(index));
										
										columns.setTaxAmount(new BigDecimal(getStringVal(cellValue)));
										if(taxheads.get(index).contains("ADVANCE")) 
											columns.setTaxAmount(columns.getTaxAmount().negate());
										columns.setCollectedAmount(BigDecimal.ZERO);
									}
								}
								if (null != columns.getTaxHeadMasterCode() && !columns.getTaxHeadMasterCode().isEmpty()) {
								if (null == excelmap.get(key))
									excelmap.put(key, new ArrayList<ExcelColumns>());
								excelmap.get(key).add(columns);
								}

							} catch (Exception e) {
								wrapper.updateMaps(ImportReportWrapper.issueFoundReport,(row.getRowNum() + 1) +  "- " + (cell.getColumnIndex()));
							}
						}

					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new CustomException("INVALID_EXCEL", "Excel data is not valid");
				}

				if (excelmap.entrySet().size() > 0) {

					for (Entry<String, List<ExcelColumns>> entry : excelmap.entrySet()) {

						Set<String> ids = new HashSet<String>();
						ids.add(entry.getKey());
						PropertyCriteria propertyCriteria = PropertyCriteria.builder().tenantId(tenantId)
								.abasPropertyids(entry.getKey()).build();
						if (propertyService
								.searchProperty(propertyCriteria, requestInfo)
								.size() > 0) {
							Property property = propertyService
									.searchProperty(propertyCriteria, requestInfo)
									.get(0);
							Assessment assessment = new Assessment();
							//JsonNode additionalDetails = om.createObjectNode();

							assessment.setTenantId(tenantId);
							assessment.setPropertyId(property.getPropertyId());
							assessment.setFinancialYear("2020-21");
							assessment.setAssessmentDate(1585679400000l);// to be verififed
							assessment.setSource(Source.LEGACY_RECORD);
							assessment.setChannel(property.getChannel());
							assessment.setStatus(property.getStatus());

							//((ObjectNode) additionalDetails).put("RequestInfo", "tt: tt");
							Demand demand = new Demand();
							demand.setTenantId(tenantId);
							demand.setConsumerCode(property.getPropertyId());
							demand.setConsumerType(property.getPropertyType());
							demand.setBusinessService("PT");
							demand.setTaxPeriodFrom(1585679400000l);// to be verified
							demand.setTaxPeriodTo(1617215399000l);// to be verified
							User payer = new User();
							payer.setUuid(property.getOwners().get(0).getUuid());// to be verified
							demand.setPayer(payer);
							List<DemandDetail> demandDetails = new ArrayList<DemandDetail>();
							for (ExcelColumns column : entry.getValue()) {
								DemandDetail detail = new DemandDetail();
								detail.setTaxHeadMasterCode(column.getTaxHeadMasterCode());
								detail.setTaxAmount(column.getTaxAmount());
								detail.setCollectionAmount(column.getCollectedAmount());
								demandDetails.add(detail);
							}
							demand.setDemandDetails(demandDetails);
							System.out.println("demand::"+demand);
							DemandRequest demandRequest = new DemandRequest();
							demandRequest.setDemands(Arrays.asList(demand));
							demandRequest.setRequestInfo(requestInfo);
							//JsonNode propertyAdditionalDetails = om.readTre
							JsonNode additionalDetails = mapper.convertValue(demandRequest,JsonNode.class);
							assessment.setAdditionalDetails(additionalDetails);

							AssessmentRequest assessmentRequest = AssessmentRequest.builder()
									.assessment(assessment).requestInfo(requestInfo)
									.build();
							Assessment assessments = null;
							
							Set<String> propertyIds= new HashSet<String>();
							propertyIds.add(property.getPropertyId());
							AssessmentSearchCriteria criteria = AssessmentSearchCriteria.builder()
									.tenantId(tenantId)
									.financialYear("2020-21")
									.propertyIds(propertyIds)
									.build();
							if(assessmentService.searchAssessments(criteria).size()>0) {
								System.out.println("in update");
								
								//Search demand exists for fin year
								StringBuilder uri = new StringBuilder(config.getEgbsHost()).append(config.getEgbsSearchDemand())
										.append("?tenantId=").append(tenantId).append("&consumerCode=").append(property.getPropertyId())
										.append("&periodFrom=").append(demand.getTaxPeriodFrom())
						                .append("&periodTo=").append(demand.getTaxPeriodTo());
								Object res;
								try {
									res = serviceRequestRepository.fetchResult(uri, RequestInfoWrapper.builder().requestInfo(requestInfo).build()).orElse(null);
								} catch (ServiceCallException e) {
									throw e;
								}
								DemandResponse demandRes = mapper.convertValue(res, DemandResponse.class);
								
								List<Demand> existingDemands = demandRes.getDemands();
								if(existingDemands!=null && existingDemands.size()!=0) {
									System.out.println("existingDemands"+existingDemands);
									Demand oldDemand = existingDemands.get(0);
									List<DemandDetail> oldDemandDetails = new ArrayList<DemandDetail>();
									oldDemandDetails = oldDemand.getDemandDetails();
									List<DemandDetail> notExistsDetails = new ArrayList<DemandDetail>();
									//Update the amount for already existing taxhead details
									for (DemandDetail demandDetail : oldDemandDetails) {
										List<DemandDetail> demDetail = demandDetails.stream()
				                                .filter(dd -> dd.getTaxHeadMasterCode().equalsIgnoreCase(demandDetail.getTaxHeadMasterCode()))
				                                .collect(Collectors.toList());
										if(demDetail.size()!=0) {
											demandDetail.setTaxAmount(demDetail.get(0).getTaxAmount());
											demandDetail.setCollectionAmount(demDetail.get(0).getCollectionAmount());
										}
										
									}
									//Add new tax head if taxhead does not exists in search demand response
									for (DemandDetail demandDetail : demandDetails) {
										List<DemandDetail> demDetail = oldDemandDetails.stream()
				                                .filter(dd -> dd.getTaxHeadMasterCode().equalsIgnoreCase(demandDetail.getTaxHeadMasterCode()))
				                                .collect(Collectors.toList());
										if(demDetail.size()==0) {

													DemandDetail detail = new DemandDetail();
													detail.setTaxHeadMasterCode(demandDetail.getTaxHeadMasterCode());
													detail.setTaxAmount(demandDetail.getTaxAmount());
													detail.setCollectionAmount(demandDetail.getCollectionAmount());
													notExistsDetails.add(detail);
												
										}
										
									}
									oldDemandDetails.addAll(notExistsDetails);
									oldDemand.setDemandDetails(oldDemandDetails);
									demandRequest.setDemands(Arrays.asList(oldDemand));
									demandRequest.setRequestInfo(requestInfo);
									JsonNode additionalDetails1 = mapper.convertValue(demandRequest,JsonNode.class);
									assessment.setAdditionalDetails(additionalDetails1);
									assessments = assessmentService.updateLegacyAssessments(assessmentRequest);
								}
								
							}
							else {
								System.out.println("in create");
								assessments = assessmentService.createLegacyAssessments(assessmentRequest);
							}
							wrapper.updateMaps(ImportReportWrapper.successReport, entry.getKey());
						} else {
							wrapper.updateMaps(ImportReportWrapper.propertyNotFoundReport, entry.getKey());
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException("INVALID_EXCEL", "Excel data is not valid");
		}
		return new ResponseEntity<>(wrapper, HttpStatus.OK);
		}
		return null;
	}

	private String getStringVal(CellValue cell) {
		return cell.getCellType() == CellType.NUMERIC ? String.valueOf((float) cell.getNumberValue())
				: cell.getStringValue();
	}
}
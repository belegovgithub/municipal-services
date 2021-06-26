package org.egov.wscalculation.consumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.tracer.model.CustomException;
import org.egov.wscalculation.config.WSCalculationConfiguration;
import org.egov.wscalculation.constants.WSCalculationConstant;
import org.egov.wscalculation.validator.WSCalculationWorkflowValidator;
import org.egov.wscalculation.web.models.BillEstimation;
import org.egov.wscalculation.web.models.Calculation;
import org.egov.wscalculation.web.models.CalculationCriteria;
import org.egov.wscalculation.web.models.CalculationReq;
import org.egov.wscalculation.producer.WSCalculationProducer;
import org.egov.wscalculation.service.DemandService;
import org.egov.wscalculation.service.MasterDataService;
import org.egov.wscalculation.service.WSCalculationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.KafkaHeaders;
@Slf4j
@Component
public class DemandGenerationConsumer {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private WSCalculationConfiguration config;

	@Autowired
	private WSCalculationServiceImpl wSCalculationServiceImpl;

	@Autowired
	private WSCalculationProducer producer;

	@Autowired
	private MasterDataService mDataService;

	@Autowired
	private WSCalculationWorkflowValidator wsCalulationWorkflowValidator;
	
	@Autowired
	private DemandService demandService;
	
	/**
	 * Listen the topic for processing the batch records.
	 * 
	 * @param records
	 *            would be calculation criteria.
	 */
	@KafkaListener(topics = {
			"${egov.watercalculatorservice.createdemand.topic}" }, containerFactory = "kafkaListenerContainerFactoryBatch")
	public void listen(final List<Message<?>> records) {
		CalculationReq calculationReq = mapper.convertValue(records.get(0).getPayload(), CalculationReq.class);

		HashMap<String, List<CalculationCriteria>> hashMapForCbs = new HashMap<String, List<CalculationCriteria>>();
		 
		records.forEach(record -> {
			try {
				CalculationReq calcReq = mapper.convertValue(record.getPayload(), CalculationReq.class);
				if(hashMapForCbs.get(calcReq.getCalculationCriteria().get(0).getTenantId())==null) {
					hashMapForCbs.put(calcReq.getCalculationCriteria().get(0).getTenantId() , new ArrayList<CalculationCriteria>());
				}
				hashMapForCbs.get(calcReq.getCalculationCriteria().get(0).getTenantId() ).addAll(calcReq.getCalculationCriteria());
			} catch (final Exception e) {
				StringBuilder builder = new StringBuilder();
				try {
					builder.append("Error while listening to value: ").append(mapper.writeValueAsString(record))
							.append(" on topic: ").append(e);
				} catch (JsonProcessingException e1) {
					log.error("KAFKA_PROCESS_ERROR", e1);
				}catch (Exception e2) {
					log.error("GENERIC_EXCEPTION", e2);
				}
				log.error(builder.toString());
			}
		});
		
		for (String tenant : hashMapForCbs.keySet()) {
			Map<String, Object> masterMap = mDataService.loadMasterData(calculationReq.getRequestInfo(),tenant);
			CalculationReq request = CalculationReq.builder().calculationCriteria(hashMapForCbs.get(tenant))
					.requestInfo(calculationReq.getRequestInfo()).isconnectionCalculation(true).build();
			generateDemandInBatch(request, masterMap, config.getDeadLetterTopicBatch());
		}
		
		
		
		log.info("Number of batch records:  " + records.size());
	}

	/**
	 * Listens on the dead letter topic of the bulk request and processes every
	 * record individually and pushes failed records on error topic
	 * 
	 * @param records
	 *            failed batch processing
	 */
	@KafkaListener(topics = {
			"${persister.demand.based.dead.letter.topic.batch}" }, containerFactory = "kafkaListenerContainerFactory")
	public void listenDeadLetterTopic(final List<Message<?>> records) {
		CalculationReq calculationReq = mapper.convertValue(records.get(0).getPayload(), CalculationReq.class);
		
		HashMap<String, List<CalculationReq>> hashMapForCbs = new HashMap<String, List<CalculationReq>>();
		int index =0;
		records.forEach(record -> {
			try {
				CalculationReq calcReq = mapper.convertValue(record.getPayload(), CalculationReq.class);
				if(hashMapForCbs.get( calcReq.getCalculationCriteria().get(0).getTenantId())==null ) {
					hashMapForCbs.put(calcReq.getCalculationCriteria().get(0).getTenantId() , new ArrayList<CalculationReq>());
				}
				hashMapForCbs.get( calcReq.getCalculationCriteria().get(0).getTenantId()).add(calcReq);
			} catch (final Exception e) {
				StringBuilder builder = new StringBuilder();
				try {
					builder.append("Error while listening to value: ").append(mapper.writeValueAsString(record))
							.append(" on dead letter topic. ").append(e);
				} catch (JsonProcessingException e1) {
					log.error("KAFKA_PROCESS_ERROR", e1);
				}catch (Exception e2) {
					log.error("GENERIC_EXCEPTION", e2);
				} 
				log.error(builder.toString() );
			}
		});
		try {
			for (String tenant : hashMapForCbs.keySet()) {
				Map<String, Object> masterMap = mDataService.loadMasterData(calculationReq.getRequestInfo(), tenant);
				hashMapForCbs.get(tenant).forEach(calcReq -> {
					try {
						calcReq.getCalculationCriteria().forEach(calcCriteria -> {
							CalculationReq request = CalculationReq.builder().calculationCriteria(Arrays.asList(calcCriteria))
									.requestInfo(calculationReq.getRequestInfo()).isconnectionCalculation(true).build();
							try {
								log.info("Generating Demand for Criteria : " + mapper.writeValueAsString(calcCriteria));
								// processing single
								generateDemandInBatch(request, masterMap,config.getDeadLetterTopicSingle());
							} catch (final Exception e) {
								StringBuilder builder = new StringBuilder();
								try {
									builder.append("Error while generating Demand in DEAD letter for Criteria: ")
											.append(mapper.writeValueAsString(calcCriteria));
								} catch (JsonProcessingException e1) {
									log.error("KAFKA_PROCESS_ERROR", e1);
								}
								log.error(builder.toString(), e);
							}
						});
					} catch (final Exception e) {
						StringBuilder builder = new StringBuilder();
						builder.append("Error while listening to value: ").append(tenant ).append(" on dead letter topic.");
						log.error(builder.toString(), e);
					}
				});
			}
		}catch (final Exception e) { 
			log.error("Error while listening to value: on dead letter topic.", e);
		}
		
		
		
		
		
	}
	
	@KafkaListener(topics = { "${persister.demand.based.newmodify.letter.topic.single}" })
	public void listen(final HashMap<String, Object> request, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {	
		try {
			log.info("Generating Demand for Criteria : " + mapper.writeValueAsString(request));
			CalculationReq calcReq = mapper.convertValue(request, CalculationReq.class);	
			Map<String, Object> masterMap = mDataService.loadMasterData(calcReq.getRequestInfo(), calcReq.getCalculationCriteria().get(0).getTenantId());
			// processing single
			//Getting water connection
			//wsCalculatorQueryBuilder.getWaterConection(calcReq.getCalculationCriteria().get(0).getConnectionNo(), calcReq.getCalculationCriteria().get(0).getTenantId());
			
			generateDemandForNewModifyConnection(calcReq, masterMap,config.getDeadLetterTopicSingle());
		} catch (final Exception e) {
			StringBuilder builder = new StringBuilder();
			try {
				builder.append("Error while generating Demand in DEAD letter for Criteria: ")
						.append(mapper.writeValueAsString(request));
			} catch (JsonProcessingException e1) {
				log.error("KAFKA_PROCESS_ERROR", e1);
			}
			log.error(builder.toString(), e);
		}
	}

	/**
	 * Generate demand in bulk on given criteria
	 * 
	 * @param request
	 *            Calculation request
	 * @param masterMap
	 *            master data
	 * @param errorTopic
	 *            error topic
	 */
	private void generateDemandInBatch(CalculationReq request, Map<String, Object> masterMap, String errorTopic) {
		try {
			for(CalculationCriteria criteria : request.getCalculationCriteria()){
				Boolean genratedemand = true;
				wsCalulationWorkflowValidator.applicationValidation(request.getRequestInfo(),criteria.getTenantId(),criteria.getConnectionNo(),genratedemand);
			}
			wSCalculationServiceImpl.bulkDemandGeneration(request, masterMap);
			String connectionNoStrings = request.getCalculationCriteria().stream()
					.map(criteria -> criteria.getConnectionNo()).collect(Collectors.toSet()).toString();
			StringBuilder str = new StringBuilder("Demand generated Successfully. For records : ")
					.append(connectionNoStrings);
			log.info(str.toString());
		} catch(CustomException cex) {
			request.setReason(cex.getMessage());
			log.error("Demand generation error: ", cex);
			producer.push(errorTopic, request);
		}
		
		catch (Exception ex) {		
			request.setReason(ex.getMessage());			
			log.error("Demand generation error: ", ex);
			producer.push(errorTopic, request);
		}

	}
	
	public void generateDemandForNewModifyConnection(CalculationReq request, Map<String, Object> masterMap, String errorTopic) {
		try {
			for(CalculationCriteria criteria : request.getCalculationCriteria()){
				Boolean genratedemand = true;
				wsCalulationWorkflowValidator.applicationValidation(request.getRequestInfo(),criteria.getTenantId(),criteria.getConnectionNo(),genratedemand);
			}
			
			log.info("Calling Demand generation==");
			BillEstimation billEstimation = new BillEstimation();
			List<Calculation> result  = wSCalculationServiceImpl.getWaterBillEstimate(request, masterMap,billEstimation);
			demandService.generateDemand(request.getRequestInfo(), result, masterMap, true);
 
			String connectionNoStrings = request.getCalculationCriteria().stream()
					.map(criteria -> criteria.getConnectionNo()).collect(Collectors.toSet()).toString();
			StringBuilder str = new StringBuilder("Demand generated Successfully. For records : ")
					.append(connectionNoStrings);
			log.info(str.toString());
		} catch(CustomException cex) {
			request.setReason(cex.getMessage());
			log.error("Demand generation error: ", cex);
			producer.push(errorTopic, request);
		}
		
		catch (Exception ex) {		
			request.setReason(ex.getMessage());			
			log.error("Demand generation error: ", ex);
			producer.push(errorTopic, request);
		}

	}
}

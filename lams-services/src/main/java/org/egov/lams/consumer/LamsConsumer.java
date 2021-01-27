package org.egov.lams.consumer;


import java.util.HashMap;

import org.egov.lams.config.LamsConfiguration;
import org.egov.lams.notification.LAMSNotificationService;
import org.egov.lams.web.models.LamsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
public class LamsConsumer {

    private LAMSNotificationService notificationService;
    private LamsConfiguration config;

    @Autowired
    public LamsConsumer(LAMSNotificationService notificationService,LamsConfiguration config) {
        this.notificationService = notificationService;
        this.config=config;
    }

    @KafkaListener(topics = {"${persister.update.lamsLR.topic}","${persister.save.lamsLR.topic}","${persister.update.lamsLR.workflow.topic}",
    		"${persister.save.lams.esign.topic}","${persister.update.lams.esign.topic}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
    	if(!(topic.equalsIgnoreCase(config.getSaveLamsEsignTopic()) || topic.equalsIgnoreCase(config.getUpdateLamsEsignTopic()))) {
        ObjectMapper mapper = new ObjectMapper();
        LamsRequest lamsRequest = new LamsRequest();
        try {
            log.info("Consuming record: " + record);
            lamsRequest = mapper.convertValue(record, LamsRequest.class);
        } catch (final Exception e) {
            log.error("Error while listening to value: " + record + " on topic: " + topic + ": " + e);
        }
        log.info(" Received: "+lamsRequest.getLeases().get(0).getApplicationNumber());
        notificationService.process(lamsRequest);
    	}
    	else
    		log.info("Consuming record: " + record);
    }
}

package org.bel.birthdeath.common.consumer;


import java.util.HashMap;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
public class CommonConsumer {

    @KafkaListener(topics = {"${persister.save.birth.topic}","${persister.update.birth.topic}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
    		log.info("Consuming record: " + record);
    }
}

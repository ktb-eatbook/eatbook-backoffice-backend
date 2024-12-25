package com.eatbook.backoffice.domain.episode.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class KafkaProducerService {

    private static final String DLQ_TOPIC = "dlq-topic"; // Dead Letter Queue 토픽

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String topic, String key) {
        log.info("Producing message to topic: {}, key: {}, message: {}", topic, key);
        kafkaTemplate.send(topic, key);
    }

    public void sendToDlq(String message) {
        log.error("Sending message to DLQ: {}", message);
        kafkaTemplate.send(DLQ_TOPIC, null, message);
    }
}
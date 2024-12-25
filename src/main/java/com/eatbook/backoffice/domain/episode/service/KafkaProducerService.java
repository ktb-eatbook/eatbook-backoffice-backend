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

    /**
     * 메시지를 전송할 때, 키와 값을 모두 포함하여 전송합니다.
     *
     * @param topic   전송할 토픽 이름
     * @param key     메시지의 키
     * @param message 메시지의 값 (JSON 형식)
     */
    public void sendMessage(String topic, String key, String message) {
        log.info("Producing message to topic: {}, key: {}, message: {}", topic, key, message);
        kafkaTemplate.send(topic, key, message);
    }

    /**
     * Dead Letter Queue로 메시지를 전송합니다.
     *
     * @param message 전송할 메시지
     */
    public void sendToDlq(String message) {
        log.error("Sending message to DLQ: {}", message);
        kafkaTemplate.send(DLQ_TOPIC, null, message);
    }
}
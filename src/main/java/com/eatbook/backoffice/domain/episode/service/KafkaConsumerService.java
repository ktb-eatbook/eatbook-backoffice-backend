package com.eatbook.backoffice.domain.episode.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.concurrent.Semaphore;

@Service
@Slf4j
@AllArgsConstructor
public class KafkaConsumerService {

    private final RedisTemplate<String, String> redisTemplate;
    private final AIService aiService; // AI 작업을 처리하는 서비스
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper; // JSON 변환을 위한 ObjectMapper 추가

    private static final int MAX_CONCURRENT_REQUESTS = 5; // 동시 요청 제한
    private static final Semaphore semaphore = new Semaphore(MAX_CONCURRENT_REQUESTS);

    private static final String DLQ_TOPIC = "dlq-topic"; // Dead Letter Queue 토픽

    @KafkaListener(topics = "speech-generation-requests", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        log.info("Consuming message: {}", record.value());

        try {
            // 메시지 파싱 (JSON 형식)
            JsonNode jsonNode = objectMapper.readTree(record.value());
            String taskId = jsonNode.get("taskId").asText();
            String data = jsonNode.has("data") ? jsonNode.get("data").asText() : "";

            log.info("Processing taskId: {}, data: {}", taskId, data);

            String status = redisTemplate.opsForValue().get("task:" + taskId + ":status");
            if ("START".equals(status)) {
                log.warn("Task already in progress, skipping duplicate: {}", taskId);
                acknowledgment.acknowledge(); // 중복 방지
                return;
            }


            // 작업 상태 "START"로 설정
            redisTemplate.opsForValue().set("task:" + taskId + ":status", "START");

            // 작업 처리
            boolean success = processWithConcurrencyControl(taskId, data);

            // 작업 성공/실패 상태 업데이트
            String finalStatus = success ? "COMPLETED" : "FAILED";
            redisTemplate.opsForValue().set("task:" + taskId + ":status", finalStatus);

            // 실패 시 Dead Letter Queue로 전송
            if (success) {
                acknowledgment.acknowledge(); // Offset Commit
            } else {
                sendToDlq(record.value()); // 실패 시 DLQ 전송
            }
        } catch (Exception e) {
            log.error("Failed to process message: {}", record.value(), e);
            // DLQ로 전송
            sendToDlq(record.value());
            // Offset 커밋을 하지 않아 메시지가 재처리되도록 함
        }
    }

    private boolean processWithConcurrencyControl(String taskId, String data) {
        try {
            semaphore.acquire(); // 동시 요청 제한
            return aiService.processTask(taskId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Semaphore interrupted for task: {}", taskId, e);
            return false;
        } finally {
            semaphore.release(); // 세마포어 반환
        }
    }

    private void sendToDlq(String message) {
        kafkaTemplate.send(DLQ_TOPIC, null, message);
        log.error("Task failed, sent to DLQ: {}", message);
    }
}
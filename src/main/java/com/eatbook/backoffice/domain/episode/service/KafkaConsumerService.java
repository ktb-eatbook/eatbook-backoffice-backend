package com.eatbook.backoffice.domain.episode.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
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

    private RedisTemplate<String, String> redisTemplate;

    private AIService aiService; // AI 작업을 처리하는 서비스

    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final int MAX_CONCURRENT_REQUESTS = 5; // 동시 요청 제한
    private static final Semaphore semaphore = new Semaphore(MAX_CONCURRENT_REQUESTS);

    private static final String TOPIC = "speech-generation-requests";

    @KafkaListener(topics = "speech-generation-requests", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        log.info("Consuming message: {}", record.value());

        // 메시지 파싱
        String message = record.value();
        log.info("Consuming message: {}", message);
        String taskId = message;
//        String taskId = extractTaskId(message);
//        String fileUrl = extractFileUrl(message);

        // 작업 상태 "START"로 설정
        redisTemplate.opsForValue().set("task:" + taskId + ":status", "START");

        // 작업 처리
        boolean success = processWithConcurrencyControl(taskId, message);

        // 작업 성공/실패 상태 업데이트
        String status = success ? "COMPLETED" : "FAILED";
        redisTemplate.opsForValue().set("task:" + taskId + ":status", status);

        // 실패 시 Dead Letter Queue로 전송
        if (!success) {
            sendToDlq(message);
        }

        // 메시지 커밋
        acknowledgment.acknowledge();
    }

    private boolean processWithConcurrencyControl(String taskId, String message) {
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
        kafkaTemplate.send(new ProducerRecord<>("dlq-topic", message));
        log.error("Task failed, sent to DLQ: {}", message);
    }

    private String extractTaskId(String message) {
        // JSON 파싱 로직
        int start = message.indexOf("\"taskId\":\"") + 10; // "taskId":" 이후 시작
        int end = message.indexOf("\":status", start); // ":status" 이전까지 추출
        if (start == -1 || end == -1) {
            log.error("Invalid taskId format in message: {}", message);
            return null; // 형식이 올바르지 않은 경우 처리
        }
        return message.substring(start, end).trim();
    }
}
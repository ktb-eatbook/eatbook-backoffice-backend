package com.eatbook.backoffice.domain.episode.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.concurrent.Semaphore;

@Service
@Slf4j
@AllArgsConstructor
public class KafkaConsumerService {

    private final RedisTemplate<String, String> redisTemplate;
    private final AIService aiService;
    private final KafkaProducerService kafkaProducerService; // KafkaProducerService 주입

    private static final int MAX_CONCURRENT_REQUESTS = 5; // 동시 요청 제한
    private static final Semaphore semaphore = new Semaphore(MAX_CONCURRENT_REQUESTS);

    @KafkaListener(topics = "speech-generation-requests", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        log.info("Consuming message: {}", record.value());

        // 메시지 파싱
        String message = record.value();
        String taskId = extractTaskId(message);

        // 작업 상태 "START"로 설정
        redisTemplate.opsForValue().set("task:" + taskId + ":status", "START");

        // 작업 처리
        boolean success = processWithConcurrencyControl(taskId, message);

        // 작업 성공/실패 상태 업데이트
        String status = success ? "COMPLETED" : "FAILED";
        redisTemplate.opsForValue().set("task:" + taskId + ":status", status);

        // 실패 시 Dead Letter Queue로 전송
        if (!success) {
            kafkaProducerService.sendToDlq(message);
        }

        // 메시지 커밋
        acknowledgment.acknowledge();
    }

    private boolean processWithConcurrencyControl(String taskId, String message) {
        try {
            semaphore.acquire(); // 동시 요청 제한
            return aiService.processTask(taskId, message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Semaphore interrupted for task: {}", taskId, e);
            return false;
        } finally {
            semaphore.release(); // 세마포어 반환
        }
    }

    private String extractTaskId(String message) {
        // JSON 파싱 로직 (간단한 문자열 추출 예시)
        int start = message.indexOf("\"taskId\":\"") + 10;
        int end = message.indexOf("\"", start);
        return message.substring(start, end);
    }
}
package com.eatbook.backoffice.domain.episode.controller;

import com.eatbook.backoffice.domain.episode.service.KafkaProducerService;
import com.eatbook.backoffice.global.response.ApiResponse;
import com.eatbook.backoffice.global.response.GlobalSuccessCode;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.eatbook.backoffice.global.response.GlobalErrorCode.NO_SUCH_TASK;
import static com.eatbook.backoffice.global.response.GlobalSuccessCode.*;

@RestController
@AllArgsConstructor
@RequestMapping("/auth/api/tasks")
public class FileUploadController {

    private KafkaProducerService kafkaProducerService;

    private RedisTemplate<String, String> redisTemplate;

    private static final String TOPIC = "speech-generation-requests";

    @PostMapping("/create/{taskId}")
    public ResponseEntity<String> createTask(@RequestBody String data, @PathVariable String taskId) {
        kafkaProducerService.sendMessage(TOPIC, taskId, data);
        // 초기 상태 설정
        redisTemplate.opsForValue().set("task:" + taskId + ":status", "PENDING");
        return ResponseEntity.ok(taskId);
    }

    @GetMapping("/{taskId}/status")
    public ResponseEntity<ApiResponse> getTaskStatus(@PathVariable String taskId) {
        // Redis에서 작업 상태 조회
        String status = redisTemplate.opsForValue().get("task:task-" + taskId + ":status");
        GlobalSuccessCode statusCode;

        // 상태 코드 매핑
        if (status == null) {
            // 상태가 없을 경우 404 응답 반환
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.of(NO_SUCH_TASK));
        }

        // 상태에 따른 GlobalSuccessCode 매핑
        statusCode = switch (status) {
            case "COMPLETED" -> JOB_SUCCESS;
            case "START" -> JOB_START;
            case "PENDING" -> JOB_PENDING;
            default -> JOB_PENDING; // 기본 상태를 PENDING으로 유지
        };

        // API 응답 생성
        ApiResponse response = ApiResponse.of(statusCode, Map.of("taskId", taskId, "status", status));

        // 응답 반환
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

}

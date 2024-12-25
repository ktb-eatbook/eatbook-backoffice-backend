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
import java.util.Set;

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
        kafkaProducerService.sendMessage(TOPIC, taskId);
        // 초기 상태 설정
        redisTemplate.opsForValue().set("task:" + taskId + ":status", "PENDING");
        return ResponseEntity.ok(taskId);
    }

    @GetMapping("/{episodeId}/status")
    public ResponseEntity<ApiResponse> getTaskStatusByEpisode(@PathVariable String episodeId) {
        // Redis에서 episodeId에 해당하는 키 검색
        String pattern = "*-" + episodeId + "-*:status";
        Set<String> matchingKeys = redisTemplate.keys(pattern);

        if (matchingKeys == null || matchingKeys.isEmpty()) {
            // 상태가 없을 경우 404 응답 반환
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.of(NO_SUCH_TASK));
        }

        // Redis에서 해당 키의 상태 조회
        String taskKey = matchingKeys.iterator().next(); // episodeId당 taskId는 하나라고 가정
        String status = redisTemplate.opsForValue().get(taskKey);

        if (status == null) {
            // 상태가 없을 경우 404 응답 반환
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.of(NO_SUCH_TASK));
        }

        // 상태에 따른 GlobalSuccessCode 매핑
        GlobalSuccessCode statusCode = switch (status) {
            case "COMPLETED" -> JOB_SUCCESS;
            case "START" -> JOB_START;
            case "PENDING" -> JOB_PENDING;
            default -> JOB_PENDING; // 기본 상태를 PENDING으로 유지
        };

        // API 응답 생성
        ApiResponse response = ApiResponse.of(statusCode, Map.of("taskKey", taskKey, "status", status));

        // 응답 반환
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

}

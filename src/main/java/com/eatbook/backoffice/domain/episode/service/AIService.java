package com.eatbook.backoffice.domain.episode.service;

import com.eatbook.backoffice.domain.episode.dto.TTSRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class AIService {

    private static final String AI_SERVER_URL = "http://192.168.2.83:8000/tts/";

    public boolean processTask(String taskId) {
        try {
            // taskId 파싱
            String[] taskComponents = taskId.split("-");
            if (taskComponents.length != 3) {
                log.error("Invalid taskId format: {}", taskId);
                return false;
            }
            String novelId = taskComponents[0];
            String episodeId = taskComponents[1];
            String fileMetadataId = taskComponents[2];

            // TTS 요청 생성
            TTSRequest ttsRequest = new TTSRequest(novelId, episodeId, fileMetadataId);

            // FastAPI 호출
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> response = restTemplate.postForObject(AI_SERVER_URL, ttsRequest, Map.class);

            // 응답 로그 출력
            log.info("Task ID: {}, AI Server response: {}", taskId, response);

            // 응답 처리
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                String audioUuid = (String) response.get("audio_uuid");
                String metadataUuid = (String) response.get("metadata_uuid");

                log.info("Task ID: {}, TTS processing started successfully. Audio UUID: {}, Metadata UUID: {}",
                        taskId, audioUuid, metadataUuid);
                return true;
            } else {
                String errorMessage = (String) response.get("error");
                log.error("Task ID: {}, TTS processing failed. Error: {}", taskId, errorMessage);
                return false;
            }
        } catch (Exception e) {
            log.error("Task ID: {}, Failed to process task via AI server", taskId, e);
            return false;
        }
    }
}
package com.eatbook.backoffice.domain.episode.service;

import com.eatbook.backoffice.domain.episode.dto.TTSRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class AIService {

    private static final String AI_SERVER_URL = "http://localhost:8000/generate-tts/";

    public boolean processTask(String episodeId, String message) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // 요청 데이터 생성
            String s3Key = "audio/" + episodeId + ".mp3";
            TTSRequest ttsRequest = new TTSRequest(message, s3Key);

            // FastAPI 호출
            String response = restTemplate.postForObject(AI_SERVER_URL, ttsRequest, String.class);
            log.info("AI Server response: {}", response);

            // 응답 확인
            return response != null && response.contains("Processing started");
        } catch (Exception e) {
            log.error("Failed to process task via AI server", e);
            return false;
        }
    }
}
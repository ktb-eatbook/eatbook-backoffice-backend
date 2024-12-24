package com.eatbook.backoffice.domain.episode.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadStatus {
    private String fileId;
    private String status; // e.g., "STARTED", "IN_PROGRESS", "COMPLETED", "FAILED"
    private String message;
    private String audioFilePath; // 생성된 음성 파일 경로 (완료 시)
}
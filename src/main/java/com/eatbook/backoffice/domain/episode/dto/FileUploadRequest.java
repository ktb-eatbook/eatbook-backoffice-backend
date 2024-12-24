package com.eatbook.backoffice.domain.episode.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadRequest {
    private String fileId;
    private String fileName;
    private String filePath;
    private String userId;
}
package com.eatbook.backoffice.domain.episode.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TTSResponse {
    private boolean success;
    private String audioUuid;
    private String metadataUuid;
    private String error;
}
package com.eatbook.backoffice.domain.episode.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TTSRequest {
    private String novelId;
    private String episodeId;
    private String scriptId;
}
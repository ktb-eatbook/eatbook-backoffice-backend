package com.eatbook.backoffice.domain.novel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NovelResponse(
        @JsonProperty("novelId")
        String novelId,

        @JsonProperty("presignedUrl")
        String presignedUrl
) {
    public NovelResponse(String novelId, String presignedUrl) {
        this.novelId = novelId;
        this.presignedUrl = presignedUrl;
    }
}

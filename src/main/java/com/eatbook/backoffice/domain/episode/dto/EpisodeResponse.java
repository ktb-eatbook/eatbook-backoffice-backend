package com.eatbook.backoffice.domain.episode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EpisodeResponse (
    @JsonProperty("episodeId")
    String episodeId,

    @JsonProperty("presignedUrl")
    String presignedUrl
) {
    public EpisodeResponse(String episodeId, String presignedUrl) {
        this.episodeId = episodeId;
        this.presignedUrl = presignedUrl;
    }
}

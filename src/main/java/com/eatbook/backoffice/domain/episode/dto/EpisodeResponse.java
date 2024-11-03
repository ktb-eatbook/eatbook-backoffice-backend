package com.eatbook.backoffice.domain.episode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EpisodeResponse (
    @JsonProperty("episodeId")
    String episodeId,

    @JsonProperty("presignedUrl")
    String preSignedUrl
) {
    public EpisodeResponse(String episodeId, String preSignedUrl) {
        this.episodeId = episodeId;
        this.preSignedUrl = preSignedUrl;
    }
}

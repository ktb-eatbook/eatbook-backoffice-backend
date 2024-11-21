package com.eatbook.backoffice.domain.novel.dto;

import java.util.List;

public record NovelEpisodeListResponse(
    String id,
    List<EpisodeInfo> episodeList
){
    public static NovelEpisodeListResponse of(
        final String id,
        final List<EpisodeInfo> episodeList
    ) {
        return new NovelEpisodeListResponse(id, episodeList);
    }
}

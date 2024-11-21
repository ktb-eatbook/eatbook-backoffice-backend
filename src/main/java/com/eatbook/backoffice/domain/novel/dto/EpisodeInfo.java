package com.eatbook.backoffice.domain.novel.dto;

public record EpisodeInfo(
    String id,
    int chapter,
    String title
){
    public static EpisodeInfo of(
        final String id,
        final int chapter,
        final String title
    ) {
        return new EpisodeInfo(id, chapter, title);
    }
}

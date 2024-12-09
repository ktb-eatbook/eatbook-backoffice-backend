package com.eatbook.backoffice.domain.episode.dto;

import com.eatbook.backoffice.entity.constant.ReleaseStatus;

import java.time.LocalDateTime;

public record EpisodeDetailResponse(
        String id,
        String title,
        int chapter,
        LocalDateTime releaseDate,
        LocalDateTime scheduledDate,
        ReleaseStatus releaseStatus,
        String novelId,
        int viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static EpisodeDetailResponse of(
            String id,
            String title,
            int chapter,
            LocalDateTime releaseDate,
            LocalDateTime scheduledDate,
            ReleaseStatus releaseStatus,
            String novelId,
            int viewCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new EpisodeDetailResponse(
                id,
                title,
                chapter,
                releaseDate,
                scheduledDate,
                releaseStatus,
                novelId,
                viewCount,
                createdAt,
                updatedAt
        );
    }
}
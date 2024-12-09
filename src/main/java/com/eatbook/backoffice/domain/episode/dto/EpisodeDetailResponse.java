package com.eatbook.backoffice.domain.episode.dto;

import com.eatbook.backoffice.entity.constant.ReleaseStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
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
}
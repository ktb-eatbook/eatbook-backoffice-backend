package com.eatbook.backoffice.domain.novel.dto;

import java.time.LocalDateTime;

public record CommentInfo(
        String id,
        int episodeNumber,
        String title,
        String userId,
        String userName,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){
    public static CommentInfo of(
            final String id,
            final int episodeNumber,
            final String title,
            final String userId,
            final String userName,
            final String content,
            final LocalDateTime createdAt,
            final LocalDateTime updatedAt
    ) {
        return new CommentInfo(id, episodeNumber, title, userId, userName, content, createdAt, updatedAt);
    }
}

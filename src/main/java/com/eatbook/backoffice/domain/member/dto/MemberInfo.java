package com.eatbook.backoffice.domain.member.dto;

import java.time.LocalDateTime;

public record MemberInfo(
    String id,
    String role,
    String nickname,
    String profileImageUrl,
    String email,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime deletedAt
){
    public static MemberInfo of(
        final String id,
        final String role,
        final String nickname,
        final String profileImageUrl,
        final String email,
        final LocalDateTime createdAt,
        final LocalDateTime updatedAt,
        final LocalDateTime deletedAt
    ) {
        return new MemberInfo(id, role, nickname, profileImageUrl, email, createdAt, updatedAt, deletedAt);
    }
}
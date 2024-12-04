package com.eatbook.backoffice.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MemberListResponse(
        @JsonProperty("totalElements")
        int totalElements,

        @JsonProperty("totalPages")
        int totalPages,

        @JsonProperty("currentPage")
        int currentPage,

        @JsonProperty("size")
        int size,

        @JsonProperty("memberList")
        List<MemberInfo> memberList
) {
    public static MemberListResponse of(
            final int totalElements,
            final int totalPages,
            final int currentPage,
            final int size,
            final List<MemberInfo> memberList
    ) {
        return new MemberListResponse(totalElements, totalPages, currentPage, size, memberList);
    }
}
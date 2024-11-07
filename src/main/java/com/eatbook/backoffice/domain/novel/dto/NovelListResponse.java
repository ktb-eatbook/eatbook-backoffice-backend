// NovelListResponse.java
package com.eatbook.backoffice.domain.novel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record NovelListResponse(
        @JsonProperty("totalElements")
        int totalElements,

        @JsonProperty("totalPages")
        int totalPages,

        @JsonProperty("currentPage")
        int currentPage,

        @JsonProperty("size")
        int size,

        @JsonProperty("novelList")
        List<NovelInfo> novelList
) {
    public static NovelListResponse of(
            final int totalElements,
            final int totalPages,
            final int currentPage,
            final int size,
            final List<NovelInfo> novelList
    ) {
        return new NovelListResponse(totalElements, totalPages, currentPage, size, novelList);
    }
}
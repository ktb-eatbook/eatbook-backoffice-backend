package com.eatbook.backoffice.domain.novel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

public record NovelDetailResponse(
        @JsonProperty("id")
        String id,

        @JsonProperty("title")
        String title,

        @JsonProperty("authorList")
        List<String> authorList,

        @JsonProperty("categoryList")
        List<String> categoryList,

        @JsonProperty("coverImageUrl")
        String coverImageUrl,

        @JsonProperty("summary")
        String summary,

        @JsonProperty("isCompleted")
        Boolean isCompleted,

        @JsonProperty("publicationYear")
        int publicationYear,

        @JsonProperty("views")
        int views,

        @JsonProperty("likes")
        int likes
) {
    @Builder
    public NovelDetailResponse(
            String id,
            String title,
            List<String> authorList,
            List<String> categoryList,
            String coverImageUrl,
            String summary,
            Boolean isCompleted,
            int publicationYear,
            int views,
            int likes
    ) {
        this.id = id;
        this.title = title;
        this.authorList = authorList;
        this.categoryList = categoryList;
        this.coverImageUrl = coverImageUrl;
        this.summary = summary;
        this.isCompleted = isCompleted;
        this.publicationYear = publicationYear;
        this.views = views;
        this.likes = likes;
    }
}
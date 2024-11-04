package com.eatbook.backoffice.domain.novel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

public record NovelRequest(
        @NotBlank(message = "책 제목은 필수입니다.")
        String title,

        @NotBlank(message = "저자명은 필수입니다.")
        String author,

        @NotBlank(message = "상세설명은 필수입니다.")
        String summary,

        @NotEmpty(message = "카테고리 리스트는 필수입니다.")
        List<String> category,

        Boolean isCompleted,

        Integer publicationYear
) {
    @Builder
    public NovelRequest(
            String title,
            String author,
            String summary,
            List<String> category,
            Boolean isCompleted,
            Integer publicationYear
    ) {
        this.title = title;
        this.author = author;
        this.summary = summary;
        this.category = category;
        this.isCompleted = isCompleted != null ? isCompleted : false;
        this.publicationYear = publicationYear != null ? publicationYear : 0;
    }
}

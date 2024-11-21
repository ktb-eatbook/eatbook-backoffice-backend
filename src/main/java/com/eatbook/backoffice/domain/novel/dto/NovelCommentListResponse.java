package com.eatbook.backoffice.domain.novel.dto;

import java.util.List;

public record NovelCommentListResponse(
        String id,
        List<CommentInfo> commentList
) {
    public static NovelCommentListResponse of(
            final String id,
            final List<CommentInfo> commentList
    ) {
        return new NovelCommentListResponse(id, commentList);
    }
}

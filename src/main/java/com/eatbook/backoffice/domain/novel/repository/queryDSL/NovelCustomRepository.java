package com.eatbook.backoffice.domain.novel.repository.queryDSL;

import com.eatbook.backoffice.domain.novel.dto.NovelCommentListResponse;
import com.eatbook.backoffice.domain.novel.dto.NovelDetailResponse;

public interface NovelCustomRepository {

    NovelDetailResponse findNovelDetailById(String novelId);

    NovelCommentListResponse findNovelCommentListById(String novelId);
}
package com.eatbook.backoffice.domain.novel.repository.queryDSL;

import com.eatbook.backoffice.domain.novel.dto.NovelDetailResponse;

public interface NovelCustomRepository {

    NovelDetailResponse findNovelDetailById(String novelId);
}
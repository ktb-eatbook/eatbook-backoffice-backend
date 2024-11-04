package com.eatbook.backoffice.domain.novel.repository;

import com.eatbook.backoffice.entity.NovelCategory;
import com.eatbook.backoffice.entity.idClass.NovelCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NovelCategoryRepository extends JpaRepository<NovelCategory, NovelCategoryId> {
}

package com.eatbook.backoffice.domain.novel.repository;

import com.eatbook.backoffice.entity.Novel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NovelRepository extends JpaRepository<Novel, String> {
    @Query("SELECT n FROM Novel n JOIN FETCH n.novelAuthors JOIN FETCH n.novelCategories")
    Page<Novel> findAllWithAuthorsAndCategories(Pageable pageable);
}

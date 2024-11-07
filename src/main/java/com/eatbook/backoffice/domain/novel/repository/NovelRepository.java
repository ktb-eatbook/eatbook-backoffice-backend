package com.eatbook.backoffice.domain.novel.repository;

import com.eatbook.backoffice.entity.Novel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NovelRepository extends JpaRepository<Novel, String> {
    @Query(
            value = "SELECT n FROM Novel n JOIN FETCH n.novelAuthors JOIN FETCH n.novelCategories",
            countQuery = "SELECT count(n) FROM Novel n")
    Page<Novel> findAllWithAuthorsAndCategories(Pageable pageable);
}

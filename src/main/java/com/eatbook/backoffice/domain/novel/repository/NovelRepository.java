package com.eatbook.backoffice.domain.novel.repository;

import com.eatbook.backoffice.domain.novel.repository.queryDSL.NovelCustomRepository;
import com.eatbook.backoffice.entity.Novel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NovelRepository extends JpaRepository<Novel, String>, NovelCustomRepository {
    @Query(
            value = "SELECT n FROM Novel n " +
                    "JOIN FETCH n.novelAuthors na " +
                    "JOIN FETCH na.author a " +
                    "JOIN FETCH n.novelCategories nc " +
                    "JOIN FETCH nc.category c",
            countQuery = "SELECT count(n) FROM Novel n")
    Page<Novel> findAllWithAuthorsAndCategories(Pageable pageable);
}

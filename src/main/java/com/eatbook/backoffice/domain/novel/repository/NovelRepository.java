package com.eatbook.backoffice.domain.novel.repository;

import com.eatbook.backoffice.domain.novel.repository.queryDSL.NovelCustomRepository;
import com.eatbook.backoffice.entity.Novel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NovelRepository extends JpaRepository<Novel, String>, NovelCustomRepository {

    @Query("SELECT n.id FROM Novel n ORDER BY n.createdAt DESC")
    Page<String> findNovelIds(Pageable pageable);

    @Query("SELECT n.id FROM Novel n " +
            "LEFT JOIN n.novelAuthors na " +
            "LEFT JOIN na.author a " +
            "LEFT JOIN n.novelCategories nc " +
            "LEFT JOIN nc.category c " +
            "WHERE LOWER(n.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "   OR LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "   OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "GROUP BY n.id " +
            "ORDER BY n.createdAt DESC")
    Page<String> findNovelIdsByQuery(@Param("query") String query, Pageable pageable);

    @Query("SELECT n FROM Novel n " +
            "LEFT JOIN FETCH n.novelAuthors na " +
            "LEFT JOIN FETCH na.author " +
            "LEFT JOIN FETCH n.novelCategories nc " +
            "LEFT JOIN FETCH nc.category " +
            "WHERE n.id IN :ids")
    List<Novel> findAllByIdsWithAuthorsAndCategories(@Param("ids") List<String> ids);
}

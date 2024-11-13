package com.eatbook.backoffice.domain.novel.repository;

import com.eatbook.backoffice.entity.Novel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NovelRepository extends JpaRepository<Novel, String> {
    @Query(
            value = "SELECT n FROM Novel n " +
                    "JOIN FETCH n.novelAuthors na " +
                    "JOIN FETCH na.author a " +
                    "JOIN FETCH n.novelCategories nc " +
                    "JOIN FETCH nc.category c",
            countQuery = "SELECT count(n) FROM Novel n")
    Page<Novel> findAllWithAuthorsAndCategories(Pageable pageable);

    @EntityGraph(attributePaths = {"novelAuthors", "novelCategories"})
    @Query(
            value = "select n from Novel n left join n.novelAuthors left join n.novelCategories",
            countQuery = "select count(n) from Novel n"
    )
    Page<Novel> findPaginatedNovelsWithAuthorsAndCategories(Pageable pageable);

    @Query("SELECT n.id FROM Novel n ORDER BY n.createdAt DESC")
    Page<String> findNovelIds(Pageable pageable);

    @Query("SELECT n FROM Novel n " +
            "LEFT JOIN FETCH n.novelAuthors na " +
            "LEFT JOIN FETCH na.author " +
            "LEFT JOIN FETCH n.novelCategories nc " +
            "LEFT JOIN FETCH nc.category " +
            "WHERE n.id IN :ids")
    List<Novel> findAllByIdsWithAuthorsAndCategories(@Param("ids") List<String> ids);
}

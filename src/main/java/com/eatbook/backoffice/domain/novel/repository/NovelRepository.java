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

    @Query("SELECT na.novel.id FROM NovelAuthor na WHERE na.author.id = :authorName ORDER BY na.novel.createdAt DESC")
    Page<String> findNovelIdsByAuthor(@Param("authorName") String authorName, Pageable pageable);

    @Query(value = "SELECT DISTINCT n.id " +
            "FROM Novel n " +
            "WHERE MATCH(n.title) AGAINST(:query IN BOOLEAN MODE) " +
            "UNION " +
            "SELECT DISTINCT n.id " +
            "FROM novel_author na " +
            "JOIN Novel n ON n.id = na.novel_id " +
            "JOIN Author a ON na.author_id = a.id " +
            "WHERE MATCH(a.name) AGAINST(:query IN BOOLEAN MODE) " +
            "UNION " +
            "SELECT DISTINCT n.id " +
            "FROM novel_category nc " +
            "JOIN Novel n ON n.id = nc.novel_id " +
            "JOIN Category c ON nc.category_id = c.id " +
            "WHERE MATCH(c.name) AGAINST(:query IN BOOLEAN MODE)",
            countQuery = "SELECT COUNT(*) FROM ( " +
                    "SELECT n.id " +
                    "FROM Novel n " +
                    "WHERE MATCH(n.title) AGAINST(:query IN BOOLEAN MODE) " +
                    "UNION " +
                    "SELECT n.id " +
                    "FROM novel_author na " +
                    "JOIN Novel n ON n.id = na.novel_id " +
                    "JOIN Author a ON na.author_id = a.id " +
                    "WHERE MATCH(a.name) AGAINST(:query IN BOOLEAN MODE) " +
                    "UNION " +
                    "SELECT n.id " +
                    "FROM novel_category nc " +
                    "JOIN Novel n ON n.id = nc.novel_id " +
                    "JOIN Category c ON nc.category_id = c.id " +
                    "WHERE MATCH(c.name) AGAINST(:query IN BOOLEAN MODE) " +
                    ") AS temp",
            nativeQuery = true)
    Page<String> findNovelIdsByQuery(@Param("query") String query, Pageable pageable);


    @Query(value = "SELECT n.id FROM Novel n " +
            "LEFT JOIN NovelAuthor na ON n.id = na.novel_id " +
            "LEFT JOIN Author a ON na.author_id = a.id " +
            "LEFT JOIN NovelCategory nc ON n.id = nc.novel_id " +
            "LEFT JOIN Category c ON nc.category_id = c.id " +
            "WHERE MATCH(n.title) AGAINST(:query IN BOOLEAN MODE) " +
            "   OR MATCH(a.name) AGAINST(:query IN BOOLEAN MODE) " +
            "   OR MATCH(c.name) AGAINST(:query IN BOOLEAN MODE) " +
            "GROUP BY n.id, n.createdAt",
            countQuery = "SELECT COUNT(DISTINCT n.id) FROM Novel n " +
                    "LEFT JOIN NovelAuthor na ON n.id = na.novel_id " +
                    "LEFT JOIN Author a ON na.author_id = a.id " +
                    "LEFT JOIN NovelCategory nc ON n.id = nc.novel_id " +
                    "LEFT JOIN Category c ON nc.category_id = c.id " +
                    "WHERE MATCH(n.title) AGAINST(:query IN BOOLEAN MODE) " +
                    "   OR MATCH(a.name) AGAINST(:query IN BOOLEAN MODE) " +
                    "   OR MATCH(c.name) AGAINST(:query IN BOOLEAN MODE)",
            nativeQuery = true)
    Page<String> findNovelIdsByQueryV2(@Param("query") String query, Pageable pageable);



    @Query("SELECT n FROM Novel n " +
            "LEFT JOIN FETCH n.novelAuthors na " +
            "LEFT JOIN FETCH na.author " +
            "LEFT JOIN FETCH n.novelCategories nc " +
            "LEFT JOIN FETCH nc.category " +
            "WHERE n.id IN :ids")
    List<Novel> findAllByIdsWithAuthorsAndCategories(@Param("ids") List<String> ids);
}

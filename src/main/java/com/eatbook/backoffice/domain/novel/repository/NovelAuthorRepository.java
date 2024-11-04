package com.eatbook.backoffice.domain.novel.repository;

import com.eatbook.backoffice.entity.NovelAuthor;
import com.eatbook.backoffice.entity.idClass.NovelAuthorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NovelAuthorRepository extends JpaRepository<NovelAuthor, NovelAuthorId> {
    @Query("SELECT na FROM NovelAuthor na " +
            "WHERE na.novel.title = :title " +
            "AND na.author.name = :authorName")
    Optional<NovelAuthor> findByNovelTitleAndAuthorName(@Param("title") String title,
                                                        @Param("authorName") String authorName);
}

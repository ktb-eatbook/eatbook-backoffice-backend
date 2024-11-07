package com.eatbook.backoffice.domain.episode.repository;

import com.eatbook.backoffice.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EpisodeRepository extends JpaRepository<Episode, String> {
    Optional<Episode> findByTitleAndNovelId(String title, String s);

    @Query("SELECT MAX(e.chapterNumber) FROM Episode e WHERE e.novel.id = :novelId")
    Integer findMaxChapterNumberByNovelId(@Param("novelId") String novelId);
}

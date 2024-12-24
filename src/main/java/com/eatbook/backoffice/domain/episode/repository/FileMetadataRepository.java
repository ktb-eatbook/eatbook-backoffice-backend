package com.eatbook.backoffice.domain.episode.repository;

import com.eatbook.backoffice.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, String> {
    @Query("select f from FileMetadata f where f.episode.id = :id")
    FileMetadata findByEpisodeId(String id);
}

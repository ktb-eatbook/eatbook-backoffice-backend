package com.eatbook.backoffice.domain.episode.repository;

import com.eatbook.backoffice.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, String> {
}

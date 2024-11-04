package com.eatbook.backoffice.domain.novel.repository;

import com.eatbook.backoffice.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorRepository extends JpaRepository<Author, String> {
    Optional<Author> findByName(String name);
}

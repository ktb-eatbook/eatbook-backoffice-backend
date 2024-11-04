package com.eatbook.backoffice.domain.novel.repository;

import com.eatbook.backoffice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, String> {
    Optional<Category> findByName(String name);
}

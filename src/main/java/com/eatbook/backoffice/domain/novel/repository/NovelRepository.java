package com.eatbook.backoffice.domain.novel.repository;

import com.eatbook.backoffice.entity.Novel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NovelRepository extends JpaRepository<Novel, String> {

}

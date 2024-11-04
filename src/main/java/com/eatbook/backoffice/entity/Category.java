package com.eatbook.backoffice.entity;

import com.eatbook.backoffice.entity.base.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "category")
public class Category extends SoftDeletableEntity {

    @Id
    @Column(length = 36)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 50)
    @NotNull
    private String name;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<NovelCategory> novelCategories = new ArrayList<>();

    @Builder
    public Category(String name) {
        this.name = name;
    }

    public void addNovel(Novel novel) {
        NovelCategory novelCategory = new NovelCategory(novel, this);
        this.novelCategories.add(novelCategory);
        novel.getNovelCategories().add(novelCategory);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id.equals(category.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}

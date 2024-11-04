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
@Table(name = "author")
public class Author extends SoftDeletableEntity {

    @Id
    @Column(length = 36)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    @NotNull
    private String name;

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private List<NovelAuthor> novelAuthors = new ArrayList<>();

    @Builder
    public Author(String name) {
        this.name = name;
    }

    public void addNovel(Novel novel) {
        NovelAuthor novelAuthor = new NovelAuthor(novel, this);
        this.novelAuthors.add(novelAuthor);
        novel.getNovelAuthors().add(novelAuthor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Author author = (Author) o;
        return id.equals(author.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

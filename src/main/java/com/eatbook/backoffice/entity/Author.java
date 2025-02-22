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
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "author")
public class Author extends SoftDeletableEntity {

    @Id
    @Column(length = 36)
//    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    @NotNull
    private String name;

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private List<NovelAuthor> novelAuthors = new ArrayList<>();

    @Builder
    public Author(String name, String id) {
        this.name = name;
        this.id = id;
    }

    @PrePersist
    public void ensureId() {
        // 이미 id가 지정되어 있다면 그대로 사용하고, null인 경우에만 생성
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
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

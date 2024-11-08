package com.eatbook.backoffice.entity;

import com.eatbook.backoffice.entity.base.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "novel")
public class Novel extends SoftDeletableEntity {

    @Id
    @Column(length = 36)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    @NotNull
    private String title;

    @Column
    private String coverImageUrl;

    @Column(length = 1000)
    private String summary;

    @Column(nullable = false)
    @NotNull
    private int viewCount=0;

    @Column(nullable = false)
    @NotNull
    private boolean isCompleted=false;

    @Column
    private int publicationYear;

    @OneToMany(mappedBy = "novel", fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<NovelAuthor> novelAuthors = new HashSet<>();

    @OneToMany(mappedBy = "novel", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Episode> episodes = new ArrayList<>();

    @OneToMany(mappedBy = "novel", fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<NovelCategory> novelCategories = new HashSet<>();

    @OneToMany(mappedBy = "novel", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Favorite> favorites = new ArrayList<>();

    @Builder
    public Novel(String title, String coverImageUrl, String summary, int viewCount, boolean isCompleted, int publicationYear) {
        this.title = title;
        this.coverImageUrl = coverImageUrl;
        this.summary = summary;
        this.viewCount = viewCount;
        this.isCompleted = isCompleted;
        this.publicationYear = publicationYear;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public void addAuthor(Author author) {
        NovelAuthor novelAuthor = new NovelAuthor(this, author);
        this.novelAuthors.add(novelAuthor);
        author.getNovelAuthors().add(novelAuthor);
    }

    public void addCategory(Category category) {
        NovelCategory novelCategory = new NovelCategory(this, category);
        this.novelCategories.add(novelCategory);
        category.getNovelCategories().add(novelCategory);
    }

    public void addEpisode(Episode episode) {
        this.episodes.add(episode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Novel novel = (Novel) o;
        return id.equals(novel.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}

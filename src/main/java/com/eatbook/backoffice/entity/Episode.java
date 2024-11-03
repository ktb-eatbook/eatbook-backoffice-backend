package com.eatbook.backoffice.entity;

import com.eatbook.backoffice.entity.base.SoftDeletableEntity;
import com.eatbook.backoffice.entity.constant.ReleaseStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "episode")
public class Episode extends SoftDeletableEntity {

    @Id
    @Column(length = 36)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    @NotNull
    private String title;

    @Column(nullable = false)
    @NotNull
    private int chapterNumber;

    @Column(nullable = false, length = 100)
    @NotNull
    @Enumerated(EnumType.STRING)
    private ReleaseStatus releaseStatus;

    @Column
    private LocalDateTime scheduledReleaseDate;

    @Column
    private LocalDateTime releasedDate;

    @Column(nullable = false)
    @NotNull
    private int viewCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    @NotNull
    private Novel novel;

    @OneToMany(mappedBy = "episode", cascade = CascadeType.ALL)
    private List<FileMetadata> fileMetadataList = new ArrayList<>();

    @Builder
    public Episode(String title, int chapterNumber, LocalDateTime scheduledReleaseDate, LocalDateTime releasedDate, ReleaseStatus releaseStatus, int viewCount, Novel novel) {
        this.title = title;
        this.chapterNumber = chapterNumber;
        this.scheduledReleaseDate = scheduledReleaseDate;
        this.releasedDate = releasedDate;
        this.releaseStatus = releaseStatus != null ? releaseStatus : ReleaseStatus.PUBLIC;
        this.viewCount = viewCount;
        this.novel = novel;
    }

    public void setReleasedDate(LocalDateTime releasedDate) {
        this.releasedDate = releasedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Episode episode = (Episode) o;
        return id.equals(episode.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}


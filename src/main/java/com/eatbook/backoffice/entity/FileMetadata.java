package com.eatbook.backoffice.entity;

import com.eatbook.backoffice.entity.base.BaseEntity;
import com.eatbook.backoffice.entity.constant.FileType;
import jakarta.persistence.*;
import lombok.*;
import org.antlr.v4.runtime.misc.NotNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "file_metadata")
public class FileMetadata extends BaseEntity {

    @Id
    @Column(length = 36)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    private FileType type;

    @Column(nullable = false)
    @NotNull
    private String path;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    @NotNull
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Episode episode;

    @Builder
    public FileMetadata(FileType type, String path, Episode episode) {
        this.type = type;
        this.path = path;
        this.episode = episode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileMetadata fileMetadata = (FileMetadata) o;
        return id.equals(fileMetadata.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public void setPath(String presignedUrl) {
        this.path = presignedUrl;
    }
}
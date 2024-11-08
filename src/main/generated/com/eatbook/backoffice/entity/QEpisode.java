package com.eatbook.backoffice.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QEpisode is a Querydsl query type for Episode
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEpisode extends EntityPathBase<Episode> {

    private static final long serialVersionUID = -1439643871L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEpisode episode = new QEpisode("episode");

    public final com.eatbook.backoffice.entity.base.QSoftDeletableEntity _super = new com.eatbook.backoffice.entity.base.QSoftDeletableEntity(this);

    public final NumberPath<Integer> chapterNumber = createNumber("chapterNumber", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final BooleanPath deleted = _super.deleted;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final ListPath<FileMetadata, QFileMetadata> fileMetadataList = this.<FileMetadata, QFileMetadata>createList("fileMetadataList", FileMetadata.class, QFileMetadata.class, PathInits.DIRECT2);

    public final StringPath id = createString("id");

    public final QNovel novel;

    public final DateTimePath<java.time.LocalDateTime> releasedDate = createDateTime("releasedDate", java.time.LocalDateTime.class);

    public final EnumPath<com.eatbook.backoffice.entity.constant.ReleaseStatus> releaseStatus = createEnum("releaseStatus", com.eatbook.backoffice.entity.constant.ReleaseStatus.class);

    public final DateTimePath<java.time.LocalDateTime> scheduledReleaseDate = createDateTime("scheduledReleaseDate", java.time.LocalDateTime.class);

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Integer> viewCount = createNumber("viewCount", Integer.class);

    public QEpisode(String variable) {
        this(Episode.class, forVariable(variable), INITS);
    }

    public QEpisode(Path<? extends Episode> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QEpisode(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QEpisode(PathMetadata metadata, PathInits inits) {
        this(Episode.class, metadata, inits);
    }

    public QEpisode(Class<? extends Episode> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.novel = inits.isInitialized("novel") ? new QNovel(forProperty("novel")) : null;
    }

}


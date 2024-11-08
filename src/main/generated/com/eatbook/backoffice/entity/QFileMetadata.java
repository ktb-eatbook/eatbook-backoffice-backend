package com.eatbook.backoffice.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFileMetadata is a Querydsl query type for FileMetadata
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFileMetadata extends EntityPathBase<FileMetadata> {

    private static final long serialVersionUID = 1060067173L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFileMetadata fileMetadata = new QFileMetadata("fileMetadata");

    public final com.eatbook.backoffice.entity.base.QBaseEntity _super = new com.eatbook.backoffice.entity.base.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QEpisode episode;

    public final StringPath id = createString("id");

    public final StringPath path = createString("path");

    public final EnumPath<com.eatbook.backoffice.entity.constant.FileType> type = createEnum("type", com.eatbook.backoffice.entity.constant.FileType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QFileMetadata(String variable) {
        this(FileMetadata.class, forVariable(variable), INITS);
    }

    public QFileMetadata(Path<? extends FileMetadata> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFileMetadata(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFileMetadata(PathMetadata metadata, PathInits inits) {
        this(FileMetadata.class, metadata, inits);
    }

    public QFileMetadata(Class<? extends FileMetadata> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.episode = inits.isInitialized("episode") ? new QEpisode(forProperty("episode"), inits.get("episode")) : null;
    }

}


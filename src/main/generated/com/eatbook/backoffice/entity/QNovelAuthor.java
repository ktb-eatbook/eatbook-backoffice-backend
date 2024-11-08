package com.eatbook.backoffice.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QNovelAuthor is a Querydsl query type for NovelAuthor
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNovelAuthor extends EntityPathBase<NovelAuthor> {

    private static final long serialVersionUID = 1339804941L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QNovelAuthor novelAuthor = new QNovelAuthor("novelAuthor");

    public final QAuthor author;

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final QNovel novel;

    public QNovelAuthor(String variable) {
        this(NovelAuthor.class, forVariable(variable), INITS);
    }

    public QNovelAuthor(Path<? extends NovelAuthor> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QNovelAuthor(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QNovelAuthor(PathMetadata metadata, PathInits inits) {
        this(NovelAuthor.class, metadata, inits);
    }

    public QNovelAuthor(Class<? extends NovelAuthor> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.author = inits.isInitialized("author") ? new QAuthor(forProperty("author")) : null;
        this.novel = inits.isInitialized("novel") ? new QNovel(forProperty("novel")) : null;
    }

}


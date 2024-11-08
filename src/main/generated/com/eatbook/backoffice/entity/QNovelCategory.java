package com.eatbook.backoffice.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QNovelCategory is a Querydsl query type for NovelCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNovelCategory extends EntityPathBase<NovelCategory> {

    private static final long serialVersionUID = 1974767616L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QNovelCategory novelCategory = new QNovelCategory("novelCategory");

    public final QCategory category;

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final QNovel novel;

    public QNovelCategory(String variable) {
        this(NovelCategory.class, forVariable(variable), INITS);
    }

    public QNovelCategory(Path<? extends NovelCategory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QNovelCategory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QNovelCategory(PathMetadata metadata, PathInits inits) {
        this(NovelCategory.class, metadata, inits);
    }

    public QNovelCategory(Class<? extends NovelCategory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new QCategory(forProperty("category")) : null;
        this.novel = inits.isInitialized("novel") ? new QNovel(forProperty("novel")) : null;
    }

}


package com.eatbook.backoffice.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QNovel is a Querydsl query type for Novel
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNovel extends EntityPathBase<Novel> {

    private static final long serialVersionUID = -958566174L;

    public static final QNovel novel = new QNovel("novel");

    public final com.eatbook.backoffice.entity.base.QSoftDeletableEntity _super = new com.eatbook.backoffice.entity.base.QSoftDeletableEntity(this);

    public final StringPath coverImageUrl = createString("coverImageUrl");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final BooleanPath deleted = _super.deleted;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final ListPath<Episode, QEpisode> episodes = this.<Episode, QEpisode>createList("episodes", Episode.class, QEpisode.class, PathInits.DIRECT2);

    public final ListPath<Favorite, QFavorite> favorites = this.<Favorite, QFavorite>createList("favorites", Favorite.class, QFavorite.class, PathInits.DIRECT2);

    public final StringPath id = createString("id");

    public final BooleanPath isCompleted = createBoolean("isCompleted");

    public final SetPath<NovelAuthor, QNovelAuthor> novelAuthors = this.<NovelAuthor, QNovelAuthor>createSet("novelAuthors", NovelAuthor.class, QNovelAuthor.class, PathInits.DIRECT2);

    public final SetPath<NovelCategory, QNovelCategory> novelCategories = this.<NovelCategory, QNovelCategory>createSet("novelCategories", NovelCategory.class, QNovelCategory.class, PathInits.DIRECT2);

    public final NumberPath<Integer> publicationYear = createNumber("publicationYear", Integer.class);

    public final StringPath summary = createString("summary");

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Integer> viewCount = createNumber("viewCount", Integer.class);

    public QNovel(String variable) {
        super(Novel.class, forVariable(variable));
    }

    public QNovel(Path<? extends Novel> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNovel(PathMetadata metadata) {
        super(Novel.class, metadata);
    }

}


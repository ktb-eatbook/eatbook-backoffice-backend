package com.eatbook.backoffice.domain.novel.repository.queryDSL;

import com.eatbook.backoffice.domain.novel.dto.CommentInfo;
import com.eatbook.backoffice.domain.novel.dto.NovelCommentListResponse;
import com.eatbook.backoffice.domain.novel.dto.NovelDetailResponse;
import com.eatbook.backoffice.domain.novel.exception.NovelNotFoundException;
import com.eatbook.backoffice.entity.Novel;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static com.eatbook.backoffice.domain.novel.response.NovelErrorCode.NOVEL_NOT_FOUND;
import static com.eatbook.backoffice.entity.QAuthor.author;
import static com.eatbook.backoffice.entity.QCategory.category;
import static com.eatbook.backoffice.entity.QComment.comment;
import static com.eatbook.backoffice.entity.QEpisode.episode;
import static com.eatbook.backoffice.entity.QFavorite.favorite;
import static com.eatbook.backoffice.entity.QNovel.novel;
import static com.eatbook.backoffice.entity.QNovelAuthor.novelAuthor;
import static com.eatbook.backoffice.entity.QNovelCategory.novelCategory;

@Repository
@AllArgsConstructor
public class NovelCustomRepositoryImpl implements NovelCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public NovelDetailResponse findNovelDetailById(String novelId) {
        validateNovelExistence(novelId);

        Novel novelEntity = jpaQueryFactory
                .selectFrom(novel)
                .leftJoin(novel.novelAuthors, novelAuthor).fetchJoin()
                .leftJoin(novelAuthor.author, author).fetchJoin()
                .leftJoin(novel.novelCategories, novelCategory).fetchJoin()
                .leftJoin(novelCategory.category, category).fetchJoin()
                .leftJoin(novel.favorites, favorite).fetchJoin()
                .where(novel.id.eq(novelId))
                .fetchOne();

        List<String> authorList = novelEntity.getNovelAuthors().stream()
                .map(novelAuthor -> novelAuthor.getAuthor().getName())
                .collect(Collectors.toList());

        List<String> categoryList = novelEntity.getNovelCategories().stream()
                .map(novelCategory -> novelCategory.getCategory().getName())
                .collect(Collectors.toList());

        int likes = novelEntity.getFavorites().size();

        return new NovelDetailResponse(
                novelEntity.getId(),
                novelEntity.getTitle(),
                authorList,
                categoryList,
                novelEntity.getCoverImageUrl(),
                novelEntity.getSummary(),
                novelEntity.isCompleted(),
                novelEntity.getPublicationYear(),
                novelEntity.getViewCount(),
                likes
        );
    }

    @Override
    public NovelCommentListResponse findNovelCommentListById(String novelId) {
        validateNovelExistence(novelId);

        List<CommentInfo> comments = jpaQueryFactory
                .select(Projections.constructor(CommentInfo.class,
                        comment.id,
                        episode.chapterNumber,
                        episode.title,
                        comment.member.id,
                        comment.member.nickname,
                        comment.content,
                        comment.createdAt,
                        comment.updatedAt
                ))
                .from(comment)
                .join(comment.episode, episode)
                .join(episode.novel, novel)
                .where(novel.id.eq(novelId))
                .orderBy(episode.chapterNumber.asc(), comment.createdAt.asc())
                .fetch();

        return NovelCommentListResponse.of(novelId, comments);
    }

    private void validateNovelExistence(String novelId) {
        Long count = jpaQueryFactory
                .select(novel.id.count())
                .from(novel)
                .where(novel.id.eq(novelId))
                .fetchOne();

        if (count == null || count == 0) {
            throw new NovelNotFoundException(NOVEL_NOT_FOUND);
        }
    }
}
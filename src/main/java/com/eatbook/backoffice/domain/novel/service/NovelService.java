package com.eatbook.backoffice.domain.novel.service;

import com.eatbook.backoffice.domain.novel.dto.NovelRequest;
import com.eatbook.backoffice.domain.novel.dto.NovelResponse;
import com.eatbook.backoffice.domain.novel.exception.NovelAlreadyExistsException;
import com.eatbook.backoffice.domain.novel.repository.*;
import com.eatbook.backoffice.entity.*;
import com.eatbook.backoffice.entity.constant.ContentType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.eatbook.backoffice.domain.novel.response.NovelErrorCode.NOVEL_ALREADY_EXISTS;
import static com.eatbook.backoffice.entity.constant.ContentType.JPEG;

/**
 * 소설을 관리하기 위한 서비스 클래스.
 *
 * @author lavin
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NovelService {

    private final NovelRepository novelRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final NovelCategoryRepository novelCategoryRepository;
    private final NovelAuthorRepository novelAuthorRepository;
    private final FileService fileService;

    private static final ContentType COVER_IMAGE_CONTENT_TYPE = JPEG;
    private static final String COVER_IMAGE_DIRECTORY = "cover";

    /**
     * 새로운 소설을 생성하고, 저자, 카테고리를 연결합니다.
     *
     * @param novelRequest 새로운 소설에 대한 정보
     * @return 생성된 소설Id와 커버 이미지 생성을 위한 presignedURL에 대한 정보
     * @throws NovelAlreadyExistsException 동일한 제목과 저자를 가진 소설이 이미 존재할 경우
     */
    @Transactional
    public NovelResponse createNovel(NovelRequest novelRequest) {
        if (novelAuthorRepository.findByNovelTitleAndAuthorName(novelRequest.title(), novelRequest.author()).isPresent()) {
            throw new NovelAlreadyExistsException(NOVEL_ALREADY_EXISTS);
        }

        Novel novel = createAndSaveNovel(novelRequest);
        Author author = findOrCreateAuthor(novelRequest.author());
        linkNovelAndAuthor(novel, author);
        linkNovelWithCategories(novel, novelRequest.category());

        String presignedUrl = fileService.getPresignUrl(novel.getId(), COVER_IMAGE_CONTENT_TYPE, COVER_IMAGE_DIRECTORY);
        return new NovelResponse(novel.getId(), presignedUrl);
    }

    /**
     * 새로운 소설을 저장합니다.
     *
     * @param novelRequest 새로운 소설에 대한 정보
     * @return 저장된 소설
     */
    private Novel createAndSaveNovel(NovelRequest novelRequest) {
        Novel novel = Novel.builder()
                .title(novelRequest.title())
                .summary(novelRequest.summary())
                .isCompleted(novelRequest.isCompleted())
                .publicationYear(novelRequest.publicationYear())
                .build();

        novelRepository.save(novel);
        log.info("새로운 소설이 생성됨: {} - 제목: {}", novel.getId(), novel.getTitle());
        return novel;
    }

    /**
     * 저자를 찾거나, 새로운 저자를 생성합니다.
     *
     * @param authorName 저자의 이름
     * @return 찾거나 생성된 저자
     */
    private Author findOrCreateAuthor(String authorName) {
        return authorRepository.findByName(authorName)
                .orElseGet(() -> {
                    Author newAuthor = authorRepository.save(Author.builder().name(authorName).build());
                    log.info("새로운 저자가 생성됨: {} - 이름: {}", newAuthor.getId(), newAuthor.getName());
                    return newAuthor;
                });
    }

    /**
     * 소설과 저자를 연결합니다.
     *
     * @param novel 소설
     * @param author 저자
     */
    private void linkNovelAndAuthor(Novel novel, Author author) {
        NovelAuthor novelAuthor = NovelAuthor.builder()
                .novel(novel)
                .author(author)
                .build();
        novelAuthorRepository.save(novelAuthor);
    }

    /**
     * 소설과 카테고리를 연결합니다.
     *
     * @param novel 소설
     * @param categoryNames 카테고리 이름 목록
     */
    private void linkNovelWithCategories(Novel novel, List<String> categoryNames) {
        int categoryIndex = 1;
        for (String categoryName : categoryNames) {
            Category category = categoryRepository.findByName(categoryName)
                    .orElseGet(() -> categoryRepository.save(Category.builder().name(categoryName).build()));

            NovelCategory novelCategory = NovelCategory.builder()
                    .novel(novel)
                    .category(category)
                    .build();
            novelCategoryRepository.save(novelCategory);

            log.info("카테고리 {}: {} - 이름: {}", categoryIndex++, category.getId(), category.getName());
        }
    }
}
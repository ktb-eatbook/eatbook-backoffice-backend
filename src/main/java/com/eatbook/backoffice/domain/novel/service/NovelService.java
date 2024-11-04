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
import org.springframework.beans.factory.annotation.Value;
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
     * 파일이 업로드될 S3 버킷의 이름입니다.
     */
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    /**
     * AWS S3 버킷의 지역입니다.a
     */
    @Value("${cloud.aws.region.static}")
    private String region;

    /**
     * 새로운 소설을 생성하고, 저자, 카테고리를 연결합니다.
     *
     * @param novelRequest 새로운 소설에 대한 정보
     * @return 생성된 소설Id와 커버 이미지 생성을 위한 presignedURL에 대한 정보
     * @throws NovelAlreadyExistsException 동일한 제목과 저자를 가진 소설이 이미 존재할 경우
     */
    @Transactional
    public NovelResponse createNovel(NovelRequest novelRequest) {
        validateNovelUniqueness(novelRequest.title(), novelRequest.author());

        Novel novel = createAndSaveNovel(novelRequest);
        Author author = findOrCreateAuthor(novelRequest.author());
        linkNovelAndAuthor(novel, author);
        linkNovelWithCategories(novel, novelRequest.category());

        String presignedUrl = fileService.getPresignUrl(novel.getId(), COVER_IMAGE_CONTENT_TYPE, COVER_IMAGE_DIRECTORY);

        return new NovelResponse(novel.getId(), presignedUrl);
    }

    /**
     * 제목과 저자를 기준으로 소설의 유일성을 검증합니다.
     * 만약 같은 제목과 저자를 가진 소설이 이미 존재할 경우, NovelAlreadyExistsException을 발생시킵니다.
     *
     * @param title, author 소설의 제목과 저자
     * @throws NovelAlreadyExistsException 동일한 제목과 저자를 가진 소설이 이미 존재할 경우
     */
    private void validateNovelUniqueness(String title, String author) {
        if (novelAuthorRepository.findByNovelTitleAndAuthorName(title, author).isPresent()) {
            throw new NovelAlreadyExistsException(NOVEL_ALREADY_EXISTS);
        }
    }

    /**
     * 새로운 소설을 저장합니다.
     *
     * @param novelRequest 새로운 소설에 대한 정보
     * @return 저장된 소설
     */
    private Novel createAndSaveNovel(NovelRequest novelRequest) {
        Novel newNovel = novelRepository.save(Novel.builder()
                .title(novelRequest.title())
                .summary(novelRequest.summary())
                .isCompleted(novelRequest.isCompleted())
                .publicationYear(novelRequest.publicationYear())
                .build());

        String coverImageUrl = getCoverImageUrl(newNovel);
        newNovel.setCoverImageUrl(coverImageUrl);

        newNovel = novelRepository.save(newNovel);

        log.info("새로운 소설이 생성됨: {} - 제목: {}", newNovel.getId(), newNovel.getTitle());
        return newNovel;
    }

    private String getCoverImageUrl(Novel newNovel) {
        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/"
                + COVER_IMAGE_DIRECTORY + "/" + newNovel.getId();
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
        novel.addAuthor(author);
        novelAuthorRepository.saveAll(novel.getNovelAuthors());
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
            novel.addCategory(category);
            log.info("카테고리 {}: {} - 이름: {}", categoryIndex++, category.getId(), category.getName());
        }
    }
}
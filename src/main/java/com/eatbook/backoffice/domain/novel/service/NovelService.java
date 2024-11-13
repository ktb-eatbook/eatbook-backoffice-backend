package com.eatbook.backoffice.domain.novel.service;

import com.eatbook.backoffice.domain.novel.dto.*;
import com.eatbook.backoffice.domain.novel.exception.NovelAlreadyExistsException;
import com.eatbook.backoffice.domain.novel.exception.PageOutOfBoundException;
import com.eatbook.backoffice.domain.novel.repository.*;
import com.eatbook.backoffice.entity.*;
import com.eatbook.backoffice.entity.constant.ContentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.eatbook.backoffice.domain.novel.response.NovelErrorCode.NOVEL_ALREADY_EXISTS;
import static com.eatbook.backoffice.domain.novel.response.NovelErrorCode.PAGE_OUT_OF_BOUNDS;
import static com.eatbook.backoffice.entity.constant.ContentType.JPEG;
import static com.eatbook.backoffice.global.utils.PathGenerator.generateRelativePath;
import static com.eatbook.backoffice.global.utils.PathGenerator.getFilePath;

/**
 * 서비스 클래스는 소설을 관리하고, 저자 및 카테고리 연결을 처리합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NovelService {

    private static final ContentType COVER_IMAGE_CONTENT_TYPE = JPEG;
    private static final String COVER_IMAGE_DIRECTORY = "cover";

    private final NovelRepository novelRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final NovelCategoryRepository novelCategoryRepository;
    private final NovelAuthorRepository novelAuthorRepository;
    private final FileService fileService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

    /**
     * 소설 생성 후, 저자와 카테고리를 연결합니다.
     *
     * @param novelRequest 새로운 소설 정보
     * @return 소설 ID와 presignedURL 정보
     * @throws NovelAlreadyExistsException 중복 소설이 있을 경우 발생
     */
    @Transactional
    public NovelResponse createNovel(NovelRequest novelRequest) {
        validateNovelUniqueness(novelRequest.title(), novelRequest.author());

        Novel novel = createAndSaveNovel(novelRequest);
        Author author = findOrCreateAuthor(novelRequest.author());
        linkNovelAndAuthor(novel, author);
        linkNovelWithCategories(novel, novelRequest.category());

        String presignedUrl = fileService.getPresignUrl(
                generateRelativePath(COVER_IMAGE_DIRECTORY, novel.getId()),
                COVER_IMAGE_CONTENT_TYPE);

        return new NovelResponse(novel.getId(), presignedUrl);
    }

    /**
     * 페이지네이션을 통해 소설 목록을 가져옵니다.
     *
     * @param page 페이지 번호 (1-indexed)
     * @param size 페이지 당 소설 수
     * @return NovelListResponse - 소설 목록과 메타 정보
     * @throws PageOutOfBoundException 페이지 범위 초과 시 발생
     */
    @Transactional(readOnly = true)
    public NovelListResponse getNovelList(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<String> novelIds = novelRepository.findNovelIds(pageable);
        List<Novel> allByIdsWithAuthorsAndCategories =
                novelRepository.findAllByIdsWithAuthorsAndCategories(novelIds.getContent());

        if (page > novelIds.getTotalPages() + 1) {
            throw new PageOutOfBoundException(PAGE_OUT_OF_BOUNDS);
        }

        List<NovelInfo> novelInfoList = allByIdsWithAuthorsAndCategories.stream()
                .map(novel -> NovelInfo.of(
                        novel.getId(),
                        novel.getTitle(),
                        getAuthorNames(novel),
                        getCategoryNames(novel),
                        novel.getCoverImageUrl()))
                .collect(Collectors.toList());

        return NovelListResponse.of(
                (int) novelIds.getTotalElements(),
                novelIds.getTotalPages(),
                novelIds.getNumber() + 1,
                novelIds.getSize(),
                novelInfoList);
    }

    /**
     * 중복 소설을 방지하기 위한 검증 메서드.
     *
     * @param title 소설 제목
     * @param author 저자
     * @throws NovelAlreadyExistsException 중복 소설이 있을 경우 발생
     */
    private void validateNovelUniqueness(String title, String author) {
        if (novelAuthorRepository.findByNovelTitleAndAuthorName(title, author).isPresent()) {
            throw new NovelAlreadyExistsException(NOVEL_ALREADY_EXISTS);
        }
    }

    /**
     * 새로운 소설을 생성하고 저장합니다.
     *
     * @param novelRequest 새로운 소설 정보
     * @return 저장된 소설 엔티티
     */
    private Novel createAndSaveNovel(NovelRequest novelRequest) {
        Novel newNovel = novelRepository.save(Novel.builder()
                .title(novelRequest.title())
                .summary(novelRequest.summary())
                .isCompleted(novelRequest.isCompleted())
                .publicationYear(novelRequest.publicationYear())
                .build());

        String coverImageUrl = getFilePath(bucketName, region, COVER_IMAGE_DIRECTORY, newNovel.getId());
        newNovel.setCoverImageUrl(coverImageUrl);
        newNovel = novelRepository.save(newNovel);

        log.info("새 소설 생성됨: ID={}, 제목={}", newNovel.getId(), newNovel.getTitle());
        return newNovel;
    }

    /**
     * 저자를 찾거나, 새로운 저자를 생성합니다.
     *
     * @param authorName 저자 이름
     * @return 생성되거나 찾은 저자 엔티티
     */
    private Author findOrCreateAuthor(String authorName) {
        return authorRepository.findByName(authorName)
                .orElseGet(() -> {
                    Author newAuthor = authorRepository.save(
                            Author.builder().name(authorName).build());
                    log.info("새 저자 생성됨: ID={}, 이름={}", newAuthor.getId(), newAuthor.getName());
                    return newAuthor;
                });
    }

    /**
     * 소설과 저자를 연결합니다.
     *
     * @param novel 소설 엔티티
     * @param author 저자 엔티티
     */
    private void linkNovelAndAuthor(Novel novel, Author author) {
        novel.addAuthor(author);
        novelAuthorRepository.saveAll(novel.getNovelAuthors());
    }

    /**
     * 소설과 카테고리를 연결합니다.
     *
     * @param novel 소설 엔티티
     * @param categoryNames 카테고리 이름 목록
     */
    private void linkNovelWithCategories(Novel novel, List<String> categoryNames) {
        int categoryIndex = 1;
        for (String categoryName : categoryNames) {
            Category category = categoryRepository.findByName(categoryName)
                    .orElseGet(() -> categoryRepository.save(Category.builder().name(categoryName).build()));
            novel.addCategory(category);
            log.info("카테고리 연결됨 {}: ID={}, 이름={}", categoryIndex++, category.getId(), category.getName());
        }
        novelCategoryRepository.saveAll(novel.getNovelCategories());
    }

    /**
     * 소설과 연관된 저자 이름 목록을 반환합니다.
     *
     * @param novel 저자 목록을 가져올 소설 엔티티
     * @return 저자 이름 목록
     */
    private List<String> getAuthorNames(Novel novel) {
        return novel.getNovelAuthors().stream()
                .map(NovelAuthor::getAuthor)
                .map(Author::getName)
                .collect(Collectors.toList());
    }

    /**
     * 소설과 연관된 카테고리 이름 목록을 반환합니다.
     *
     * @param novel 카테고리 목록을 가져올 소설 엔티티
     * @return 카테고리 이름 목록
     */
    private List<String> getCategoryNames(Novel novel) {
        return novel.getNovelCategories().stream()
                .map(NovelCategory::getCategory)
                .map(Category::getName)
                .collect(Collectors.toList());
    }
}
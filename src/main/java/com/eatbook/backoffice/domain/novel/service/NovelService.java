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

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

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

        String presignedUrl = fileService.getPresignUrl(generateRelativePath(COVER_IMAGE_DIRECTORY, novel.getId()), COVER_IMAGE_CONTENT_TYPE);

        return new NovelResponse(novel.getId(), presignedUrl);
    }

    /**
     * 지정된 페이지에 있는 소설 목록과 관련된 저자, 카테고리를 가져옵니다.
     *
     * @param page 검색할 페이지 번호 (1-indexed)
     * @param size 페이지 당 소설 수
     * @return {@link NovelListResponse} - 총 소설 수, 총 페이지 수, 현재 페이지 번호, 페이지 당 소설 수, 소설 정보 목록
     * @throws PageOutOfBoundException 지정된 페이지가 총 페이지 수를 초과할 경우
     */
    @Transactional(readOnly = true)
    public NovelListResponse getNovelList(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<String> novelIds = novelRepository.findNovelIds(pageable);
        List<Novel> allByIdsWithAuthorsAndCategories = novelRepository.findAllByIdsWithAuthorsAndCategories(novelIds.getContent());

        if (page > novelIds.getTotalPages() + 1) {
            throw new PageOutOfBoundException(PAGE_OUT_OF_BOUNDS);
        }

        List<NovelInfo> novelInfoList = allByIdsWithAuthorsAndCategories.stream()
                .map(novel -> NovelInfo.of(
                        novel.getId(),
                        novel.getTitle(),
                        getAuthorNames(novel),
                        getCategoryNames(novel),
                        novel.getCoverImageUrl()
                ))
                .collect(Collectors.toList());

        return NovelListResponse.of(
                (int) novelIds.getTotalElements(),
                novelIds.getTotalPages(),
                novelIds.getNumber() + 1,
                novelIds.getSize(),
                novelInfoList
        );
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

        String coverImageUrl = getFilePath(
                bucketName,
                region,
                COVER_IMAGE_DIRECTORY,
                newNovel.getId()
        );

        newNovel.setCoverImageUrl(coverImageUrl);
        newNovel = novelRepository.save(newNovel);

        log.info("새로운 소설이 생성됨: {} - 제목: {}", newNovel.getId(), newNovel.getTitle());
        return newNovel;
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
        novelCategoryRepository.saveAll(novel.getNovelCategories());
    }

    /**
     * 주어진 소설과 연관된 저자 이름 목록을 반환합니다.
     *
     * @param novel 저자 이름을 추출할 소설 엔티티
     * @return 소설과 연관된 저자 이름 목록
     */
    private List<String> getAuthorNames(Novel novel) {
        return novel.getNovelAuthors().stream()
                .map(NovelAuthor::getAuthor)
                .map(Author::getName)
                .collect(Collectors.toList());
    }

    /**
     주어진 소설과 관련된 카테고리 이름 목록을 반환합니다.
     @param novel 카테고리 이름을 추출할 소설 엔티티
     @return 소설과 관련된 카테고리 이름 목록
     */
    private List<String> getCategoryNames(Novel novel) {
        return novel.getNovelCategories().stream()
                .map(NovelCategory::getCategory)
                .map(Category::getName)
                .collect(Collectors.toList());
    }
}
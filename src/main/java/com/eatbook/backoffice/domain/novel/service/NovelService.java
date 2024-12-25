package com.eatbook.backoffice.domain.novel.service;

import com.eatbook.backoffice.domain.novel.dto.*;
import com.eatbook.backoffice.domain.novel.exception.NovelAlreadyExistsException;
import com.eatbook.backoffice.domain.novel.exception.NovelNotFoundException;
import com.eatbook.backoffice.domain.novel.repository.*;
import com.eatbook.backoffice.entity.*;
import com.eatbook.backoffice.entity.constant.ContentType;
import com.eatbook.backoffice.global.exception.exceptions.PageOutOfBoundException;
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
import static com.eatbook.backoffice.domain.novel.response.NovelErrorCode.NOVEL_NOT_FOUND;
import static com.eatbook.backoffice.entity.constant.ContentType.JPEG;
import static com.eatbook.backoffice.global.response.GlobalErrorCode.PAGE_OUT_OF_BOUNDS;
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
    private static final String NOVEL_DIRECTORY = "novels";
    private static final String COVER_IMAGE_DIRECTORY = "cover";

    private final NovelRepository novelRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final NovelCategoryRepository novelCategoryRepository;
    private final NovelAuthorRepository novelAuthorRepository;
    private final FileService fileService;

    @Value("${cloud.aws.s3.bucket.public}")
    private String publicBucket;

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
                generateRelativePath(NOVEL_DIRECTORY, novel.getId(), COVER_IMAGE_DIRECTORY, novel.getId()),
                COVER_IMAGE_CONTENT_TYPE, publicBucket);

        return new NovelResponse(novel.getId(), presignedUrl);
    }

    /**
     * 지정된 소설 ID에 해당하는 소설을 수정합니다.
     *
     * @param novelId 수정할 소설의 ID.
     * @param novelUpdateRequest 소설 수정 요청 데이터.
     * @return 수정된 소설 상세 정보를 포함하는 {@link NovelDetailResponse}.
     * @throws NovelNotFoundException 소설이 존재하지 않을 경우 발생.
     */
    @Transactional
    public NovelResponse updateNovel(String novelId, NovelRequest novelUpdateRequest) {
        // 소설 조회
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new NovelNotFoundException(NOVEL_NOT_FOUND));

        // 소설 정보 업데이트
        novel.setTitle(novelUpdateRequest.title());
        novel.setSummary(novelUpdateRequest.summary());
        novel.setPublicationYear(novelUpdateRequest.publicationYear());
        novel.setIsCompleted(novelUpdateRequest.isCompleted());
        novel.setIsCompleted(novelUpdateRequest.isCompleted());
        novel.setPublicationYear(novelUpdateRequest.publicationYear());

        // 저자 업데이트
        Author author = findOrCreateAuthor(novelUpdateRequest.author());
        novel.clearAuthors(); // 기존 저자 연결 제거
        linkNovelAndAuthor(novel, author);

        // 카테고리 업데이트
        novel.clearCategories(); // 기존 카테고리 연결 제거
        linkNovelWithCategories(novel, novelUpdateRequest.category());

        log.info("소설 수정됨: ID={}, 제목={}", novel.getId(), novel.getTitle());

        String presignedUrl = fileService.getPresignUrl(
                generateRelativePath(NOVEL_DIRECTORY, novel.getId(), NOVEL_DIRECTORY, novel.getId(), COVER_IMAGE_DIRECTORY, novel.getId()),
                COVER_IMAGE_CONTENT_TYPE, publicBucket);

        return new NovelResponse(novel.getId(), presignedUrl);
    }

    /**
     * 제목과 저자를 기준으로 소설의 유일성을 검증합니다.
     * 만약 같은 제목과 저자를 가진 소설이 이미 존재할 경우, NovelAlreadyExistsException을 발생시킵니다.
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
        return getNovelListResponse(page, novelIds);
    }

    /**
     * 소설 Id로 소설에 대한 상세 정보를 가져옵니다.
     *
     * @param novelId 소설 Id
     * @return {@link NovelDetailResponse} 객체로, 소설에 대한 상세 정보를 담고 있습니다.
     */
    @Transactional(readOnly = true)
    public NovelDetailResponse getNovelDetail(String novelId) {
        return novelRepository.findNovelDetailById(novelId);
    }

    /**
     * 소설 Id로 소설에 대한 댓글 목록을 가져옵니다.
     *
     * @param novelId 소설 Id
     * @return {@link NovelCommentListResponse} 객체로, 지정된 소설에 대한 댓글 목록을 포함합니다.
     */
    @Transactional(readOnly = true)
    public NovelCommentListResponse getNovelComments(String novelId) {
        return novelRepository.findNovelCommentListById(novelId);
    }

    /**
     * 지정된 소설에 대한 에피소드 목록을 가져옵니다.
     *
     * @param novelId 소설의 고유 식별자
     * @return 지정된 소설에 대한 에피소드 목록이 포함된 {@link NovelEpisodeListResponse} 객체
     *         에피소드가 없으면 빈 목록을 반환합니다.
     */
    @Transactional(readOnly = true)
    public NovelEpisodeListResponse getNovelEpisodes(String novelId) {
        return novelRepository.findNovelEpisodeListById(novelId);
    }

    /**
     * 지정된 검색어로 소설을 검색하고, 페이지네이션된 결과를 반환합니다.
     *
     * @param query 검색할 키워드
     * @param page 페이지 번호 (1-indexed)
     * @param size 페이지 당 소설 수
     * @return {@link NovelListResponse} 객체로, 검색된 소설 목록과 메타데이터를 포함합니다.
     *         검색 결과가 없거나, 페이지 번호가 범위를 초과할 경우, 빈 목록과 메타데이터를 반환합니다.
     */
    @Transactional(readOnly = true)
    public NovelListResponse searchNovels(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<String> novelIdsPage = novelRepository.findNovelIdsByQuery(query, pageable);

        return getNovelListResponse(page, novelIdsPage);
    }

    /**
     * 지정된 페이지와 크기에 따라 소설 목록과 관련 메타데이터를 가져옵니다.
     *
     * @param page 페이지 번호 (1-indexed)
     * @param novelIdsPage 소설 ID로 페이징된 결과
     * @return {@link NovelListResponse} 객체로, 소설 목록과 메타데이터를 포함합니다.
     * @throws PageOutOfBoundException 페이지 번호가 범위를 초과할 경우 발생
     */
    private NovelListResponse getNovelListResponse(int page, Page<String> novelIdsPage) {
        // 소설 ID를 사용하여 소설, 저자, 카테고리를 함께 가져옵니다.
        List<Novel> novels = novelRepository.findAllByIdsWithAuthorsAndCategories(novelIdsPage.getContent());

        // 요청한 페이지가 존재하지 않는 경우 예외를 발생시킵니다.
        if (page > novelIdsPage.getTotalPages() + 1) {
            throw new PageOutOfBoundException(PAGE_OUT_OF_BOUNDS);
        }

        List<NovelInfo> novelInfoList = novels.stream()
                .map(novel -> NovelInfo.of(
                        novel.getId(),
                        novel.getTitle(),
                        getAuthorNames(novel),
                        getCategoryNames(novel),
                        novel.getCoverImageUrl()))
                .collect(Collectors.toList());

        return NovelListResponse.of(
                (int) novelIdsPage.getTotalElements(),
                novelIdsPage.getTotalPages(),
                novelIdsPage.getNumber() + 1,
                novelIdsPage.getSize(),
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

        String coverImageUrl = getFilePath(publicBucket, region, NOVEL_DIRECTORY, newNovel.getId(), COVER_IMAGE_DIRECTORY, newNovel.getId());
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

    /**
     * 지정된 소설 ID에 해당하는 소설을 삭제합니다.
     *
     * @param novelId 삭제할 소설의 ID.
     */
    @Transactional
    public void deleteNovel(String novelId) {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new NovelNotFoundException(NOVEL_NOT_FOUND));
        novelRepository.delete(novel);
    }
}
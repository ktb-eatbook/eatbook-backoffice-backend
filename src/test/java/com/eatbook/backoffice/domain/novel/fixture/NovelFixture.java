package com.eatbook.backoffice.domain.novel.fixture;

import com.eatbook.backoffice.domain.novel.dto.NovelDetailResponse;
import com.eatbook.backoffice.domain.novel.dto.NovelRequest;
import com.eatbook.backoffice.entity.Novel;
import org.springframework.data.domain.*;

import java.lang.reflect.Field;
import java.util.List;

public class NovelFixture {

    public static final String title = "Valid Title";
    public static final String author = "Valid Author";
    public static final List<String> authorList = List.of("Valid Author1", "Valid Author2");
    public static final String summary = "Valid Summary";
    public static final List<String> category = List.of("Valid Category");
    public static final boolean isCompleted = true;
    public static final int publicationYear = 1800;
    public static final String coverImageUrl = "https://cover-image-url.com";
    public static final String testId = "2ed5d018-1499-407f-a73f-23ab142ba593";
    public static final String invalidId = "nonexistent-id";
    public static final int page = 1;
    public static final int overPage = 100;
    public static final int size = 2;
    public static List<Novel> novels;


    // 헬퍼 메서드: 테스트용 Novel ID 설정
    public static Novel createNovelWithId(String id, String title, String summary, int publicationYear) {
        Novel novel = Novel.builder()
                .title(title)
                .summary(summary)
                .publicationYear(publicationYear)
                .build();

        // 리플렉션을 사용하여 id 설정
        try {
            Field idField = Novel.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(novel, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return novel;
    }

    // 헬퍼 메서드: 테스트용 NovelRequest 생성
    public static NovelRequest getNovelRequest() {
        NovelRequest novelRequest = NovelRequest.builder()
                .title(title)
                .author(author)
                .summary(summary)
                .category(category)
                .isCompleted(isCompleted)
                .publicationYear(publicationYear)
                .build();
        return novelRequest;
    }

    // 헬퍼 메서드: 테스트용 Novel 생성
    public static void setUpNovelList() {
        // Mock novel list setup
        novels = List.of(
                NovelFixture.createNovelWithId("test-id-1", "title-1", summary, publicationYear),
                NovelFixture.createNovelWithId("test-id-2", "title-2", summary, publicationYear),
                NovelFixture.createNovelWithId("test-id-3", "title-3", summary, publicationYear),
                NovelFixture.createNovelWithId("test-id-4", "title-4", summary, publicationYear),
                NovelFixture.createNovelWithId("test-id-5", "title-5", summary, publicationYear)
        );
    }

    // 헬퍼 메서드: 테스트용 Page<Novel> 생성
    public static Page<Novel> createPaginatedNovels(int page, int size, List<Novel> novels) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return new PageImpl<>(novels.subList(0, Math.min(size, novels.size())), pageable, novels.size());
    }

    // 헬퍼 메서드: 테스트용 Page<String> 생성
    public static Page<String> createPaginatedIds(int page, int size, List<String> ids) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        int start = Math.min((page - 1) * size, ids.size());
        int end = Math.min(start + size, ids.size());
        return new PageImpl<>(ids.subList(start, end), pageable, ids.size());
    }

    // 헬퍼 메서드: 테스트용 NovelDetailResponse 생성
    public static NovelDetailResponse createDetailResponse(String novelId) {
        return NovelDetailResponse.builder()
                .id(novelId)
                .title(title)
                .authorList(authorList)
                .categoryList(category)
                .coverImageUrl(coverImageUrl)
                .summary(summary)
                .isCompleted(isCompleted)
                .publicationYear(publicationYear)
                .views(100)
                .likes(50)
                .build();
    }
}

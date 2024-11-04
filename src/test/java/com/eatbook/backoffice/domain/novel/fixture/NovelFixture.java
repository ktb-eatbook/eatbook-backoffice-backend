package com.eatbook.backoffice.domain.novel.fixture;

import com.eatbook.backoffice.domain.novel.dto.NovelRequest;
import com.eatbook.backoffice.entity.Novel;

import java.lang.reflect.Field;
import java.util.List;

public class NovelFixture {

    public static final String title = "Valid Title";
    public static final String author = "Valid Author";
    public static final String summary = "Valid Summary";
    public static final List<String> category = List.of("Valid Category");
    public static final boolean isCompleted = true;
    public static final int publicationYear = 1800;
    public static final String testId = "2ed5d018-1499-407f-a73f-23ab142ba593";

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
}

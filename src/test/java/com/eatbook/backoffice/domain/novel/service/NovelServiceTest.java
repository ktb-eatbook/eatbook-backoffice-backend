package com.eatbook.backoffice.domain.novel.service;

import com.eatbook.backoffice.domain.novel.dto.NovelRequest;
import com.eatbook.backoffice.domain.novel.dto.NovelResponse;
import com.eatbook.backoffice.domain.novel.exception.NovelAlreadyExistsException;
import com.eatbook.backoffice.domain.novel.repository.*;
import com.eatbook.backoffice.entity.*;
import com.eatbook.backoffice.entity.constant.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static com.eatbook.backoffice.domain.novel.response.NovelErrorCode.NOVEL_ALREADY_EXISTS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NovelServiceTest {

    @InjectMocks
    private NovelService novelService;

    @Mock
    private NovelRepository novelRepository;
    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private NovelCategoryRepository novelCategoryRepository;
    @Mock
    private NovelAuthorRepository novelAuthorRepository;
    @Mock
    private FileService fileService;

    private final String title = "Valid Title";
    private final String author = "Valid Author";
    private final String summary = "Valid Summary";
    private final List<String> category = List.of("Valid Category");
    private final boolean isCompleted = true;
    private final int publicationYear = 1800;
    private final String testId = "2ed5d018-1499-407f-a73f-23ab142ba593";

    @Test
    void shouldThrowNovelAlreadyExistsExceptionWhenTryingToCreateNovelWithSameTitleAndAuthor() {
        // given
        NovelRequest novelRequest = NovelRequest.builder()
                .title(title)
                .author(author)
                .summary(summary)
                .category(category)
                .isCompleted(isCompleted)
                .publicationYear(publicationYear)
                .build();

        when(novelAuthorRepository.findByNovelTitleAndAuthorName(title, author))
                .thenReturn(Optional.of(NovelAuthor.builder().build()));

        // when
        NovelAlreadyExistsException exception = assertThrows(NovelAlreadyExistsException.class, () -> novelService.createNovel(novelRequest));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(NOVEL_ALREADY_EXISTS);

        // Repository와 관련된 save 메서드가 호출되지 않았는지 확인
        verify(novelRepository, never()).save(any(Novel.class));

        // novelAuthorRepository의 메서드가 한 번만 호출되었는지 확인
        verify(novelAuthorRepository, times(1)).findByNovelTitleAndAuthorName(anyString(), anyString());

        // 파일 서비스 관련 메서드가 호출되지 않았는지 확인
        verify(fileService, never()).getPresignUrl(anyString(), any(ContentType.class), anyString());
    }

    @Test
    void shouldCreateNovelSuccessfullyWhenAllInputsAreValid() {
        // given
        NovelRequest novelRequest = NovelRequest.builder()
                .title(title)
                .author(author)
                .summary(summary)
                .category(category)
                .isCompleted(isCompleted)
                .publicationYear(publicationYear)
                .build();

        when(authorRepository.findByName(anyString()))
                .thenReturn(Optional.empty());

        when(categoryRepository.findByName(anyString()))
                .thenReturn(Optional.empty());

        when(novelRepository.save(any(Novel.class)))
                .thenAnswer(invocation -> {
                    Novel novel = invocation.getArgument(0);
                    Novel novelWithId = createNovelWithId(testId, novel.getTitle(), novel.getSummary(), novel.getPublicationYear());
                    return novelWithId;
                });

        when(authorRepository.save(any(Author.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(novelCategoryRepository.save(any(NovelCategory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(novelAuthorRepository.save(any(NovelAuthor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(fileService.getPresignUrl(any(), any(ContentType.class), anyString()))
                .thenReturn("presignedUrl");

        // When
        NovelResponse novelResponse = novelService.createNovel(novelRequest);

        // Then
        assertThat(novelResponse.novelId()).isNotNull();
        assertThat(novelResponse.presignedUrl()).isEqualTo("presignedUrl");

        // Repository와 관련된 save 메서드가 2번씩 호출되었는지 확인
        verify(novelRepository, times(2)).save(any(Novel.class));

        // 파일 서비스 관련 메서드가 한 번만 호출되었는지 확인
        verify(fileService, times(1)).getPresignUrl(anyString(), any(ContentType.class), anyString());
    }

    @Test
    void shouldCreateNovelSuccessfullyWhenTitleIsSameButAuthorIsDifferent() {
        // given

        NovelRequest novelRequest = NovelRequest.builder()
                .title(title)
                .author("New Author")
                .summary(summary)
                .category(category)
                .isCompleted(isCompleted)
                .publicationYear(publicationYear)
                .build();

        when(novelAuthorRepository.findByNovelTitleAndAuthorName(title, "New Author"))
                .thenReturn(Optional.empty()); // 새로운 작가의 경우 같은 제목의 소설이 없음

        when(authorRepository.findByName("New Author"))
                .thenReturn(Optional.empty()); // 새로운 작가 생성

        when(categoryRepository.findByName(anyString()))
                .thenReturn(Optional.empty());

        when(novelRepository.save(any(Novel.class)))
                .thenAnswer(invocation -> {
                    Novel novel = invocation.getArgument(0);
                    return createNovelWithId("test-id", novel.getTitle(), novel.getSummary(), novel.getPublicationYear());
                });

        when(authorRepository.save(any(Author.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(novelCategoryRepository.save(any(NovelCategory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(novelAuthorRepository.save(any(NovelAuthor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(fileService.getPresignUrl(any(), any(ContentType.class), anyString()))
                .thenReturn("presignedUrl");

        // When
        NovelResponse novelResponse = novelService.createNovel(novelRequest);

        // Then
        assertThat(novelResponse.novelId()).isNotNull();
        assertThat(novelResponse.presignedUrl()).isEqualTo("presignedUrl");

        // Repository와 관련된 save 메서드가 2번씩 호출되었는지 확인
        verify(novelRepository, times(2)).save(any(Novel.class));

        // 파일 서비스 관련 메서드가 한 번만 호출되었는지 확인
        verify(fileService, times(1)).getPresignUrl(anyString(), any(ContentType.class), anyString());
    }

    @Test
    void shouldCreateNovelSuccessfullyWhenCategoryAlreadyExists() {
        // given
        List<String> newCategory = List.of("Category1", "Category4");

        NovelRequest novelRequest = NovelRequest.builder()
                .title(title)
                .author(author)
                .summary(summary)
                .category(newCategory)
                .isCompleted(true)
                .publicationYear(1800)
                .build();

        when(authorRepository.findByName(anyString()))
                .thenReturn(Optional.empty());

        when(categoryRepository.findByName("Category1"))
                .thenReturn(Optional.of(Category.builder().name("Category1").build()));

        when(categoryRepository.findByName("Category4"))
                .thenReturn(Optional.empty());

        when(novelRepository.save(any(Novel.class)))
                .thenAnswer(invocation -> {
                    Novel novel = invocation.getArgument(0);
                    return createNovelWithId("test-id", novel.getTitle(), novel.getSummary(), novel.getPublicationYear());
                });

        when(authorRepository.save(any(Author.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(novelCategoryRepository.save(any(NovelCategory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(novelAuthorRepository.save(any(NovelAuthor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(fileService.getPresignUrl(any(), any(ContentType.class), anyString()))
                .thenReturn("presignedUrl");

        // When
        NovelResponse novelResponse = novelService.createNovel(novelRequest);

        // Then
        assertThat(novelResponse.novelId()).isNotNull();
        assertThat(novelResponse.presignedUrl()).isEqualTo("presignedUrl");

        // Repository와 관련된 save 메서드가 2번씩 호출되었는지 확인
        verify(novelRepository, times(2)).save(any(Novel.class));
        // 카테고리 저장 메서드가 번 호출되었는지 확인
        verify(novelCategoryRepository, times(newCategory.size())).save(any(NovelCategory.class));

        // 파일 서비스 관련 메서드가 한 번만 호출되었는지 확인
        verify(fileService, times(1)).getPresignUrl(anyString(), any(ContentType.class), anyString());
    }


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
}
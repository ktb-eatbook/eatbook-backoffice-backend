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

import static com.eatbook.backoffice.domain.novel.fixture.NovelFixture.*;
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

    @Test
    void should_ThrowNovelAlreadyExistsException_When_TryingToCreateNovelWithSameTitleAndAuthor() {
        // given
        NovelRequest novelRequest = getNovelRequest();

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
    void should_CreateNovelSuccessfully_When_AllInputsAreValid() {
        // given
        NovelRequest novelRequest = getNovelRequest();

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
    void should_CreateNovelSuccessfully_When_TitleIsSameButAuthorIsDifferent() {
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
    void should_CreateNovelSuccessfully_When_CategoryAlreadyExists() {
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

        verify(novelCategoryRepository, times(1)).saveAll(anyList());

        // 파일 서비스 관련 메서드가 한 번만 호출되었는지 확인
        verify(fileService, times(1)).getPresignUrl(anyString(), any(ContentType.class), anyString());
    }
}
package com.eatbook.backoffice.domain.novel.service;

import com.eatbook.backoffice.domain.novel.dto.NovelListResponse;
import com.eatbook.backoffice.domain.novel.dto.NovelRequest;
import com.eatbook.backoffice.domain.novel.dto.NovelResponse;
import com.eatbook.backoffice.domain.novel.exception.NovelAlreadyExistsException;
import com.eatbook.backoffice.domain.novel.exception.PageOutOfBoundException;
import com.eatbook.backoffice.domain.novel.repository.*;
import com.eatbook.backoffice.entity.Author;
import com.eatbook.backoffice.entity.Category;
import com.eatbook.backoffice.entity.Novel;
import com.eatbook.backoffice.entity.NovelAuthor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static com.eatbook.backoffice.domain.novel.fixture.NovelFixture.*;
import static com.eatbook.backoffice.domain.novel.response.NovelErrorCode.NOVEL_ALREADY_EXISTS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

        // When
        NovelResponse novelResponse = novelService.createNovel(novelRequest);

        // Then
        assertThat(novelResponse.novelId()).isNotNull();
        // Repository와 관련된 save 메서드가 2번씩 호출되었는지 확인
        verify(novelRepository, times(2)).save(any(Novel.class));
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

        // When
        NovelResponse novelResponse = novelService.createNovel(novelRequest);

        // Then
        assertThat(novelResponse.novelId()).isNotNull();

        // Repository와 관련된 save 메서드가 2번씩 호출되었는지 확인
        verify(novelRepository, times(2)).save(any(Novel.class));
    }

    @Test
    void should_ReturnNovelListWithCorrectPagination_when_ThereAreMultiplePages() {
        // given
        setUpNovelList();
        List<String> novelIds = novels.stream().map(Novel::getId).toList();
        Page<String> paginatedIds = createPaginatedIds(page, size, novelIds);
        Mockito.when(novelRepository.findNovelIds(any(Pageable.class)))
                .thenReturn(paginatedIds);

        Mockito.when(novelRepository.findAllByIdsWithAuthorsAndCategories(anyList()))
                .thenReturn(novels);

        int totalElements = novels.size();
        int expectedTotalPages = (totalElements + size - 1) / size;

        // when
        NovelListResponse result = novelService.getNovelList(page, size);

        // then
        assertEquals(totalElements, result.totalElements());
        assertEquals(expectedTotalPages, result.totalPages());
        assertEquals(page, result.currentPage());
        assertEquals(size, result.size());
    }

    @Test
    void should_ThrowPageOutOfBoundException_when_PageExceedsTotalPages() {
        // given
        setUpNovelList();
        List<String> novelIds = novels.stream().map(Novel::getId).toList();
        Page<String> paginatedIds = createPaginatedIds(page, size, novelIds);

        Mockito.when(novelRepository.findNovelIds(any(Pageable.class)))
                .thenReturn(paginatedIds);

        // when, then
        assertThrows(PageOutOfBoundException.class, () -> novelService.getNovelList(overPage, size));
    }
}
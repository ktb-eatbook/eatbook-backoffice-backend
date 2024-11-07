package com.eatbook.backoffice.domain.episode.service;

import com.eatbook.backoffice.domain.episode.dto.EpisodeRequest;
import com.eatbook.backoffice.domain.episode.dto.EpisodeResponse;
import com.eatbook.backoffice.domain.episode.exception.EpisodeAlreadyExistsException;
import com.eatbook.backoffice.domain.episode.repository.EpisodeRepository;
import com.eatbook.backoffice.domain.episode.repository.FileMetadataRepository;
import com.eatbook.backoffice.domain.novel.repository.NovelRepository;
import com.eatbook.backoffice.domain.novel.service.FileService;
import com.eatbook.backoffice.entity.Episode;
import com.eatbook.backoffice.entity.FileMetadata;
import com.eatbook.backoffice.entity.Novel;
import com.eatbook.backoffice.entity.constant.ContentType;
import com.eatbook.backoffice.entity.constant.ReleaseStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.eatbook.backoffice.domain.episode.fixture.EpisodeFixture.*;
import static com.eatbook.backoffice.domain.episode.response.EpisodeErrorCode.EPISODE_TITLE_DUPLICATED;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EpisodeServiceTest {

    private static final ContentType EPISODE_CONTENT_TYPE = ContentType.TXT;
    @Mock
    private EpisodeRepository episodeRepository;

    @Mock
    private NovelRepository novelRepository;

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @Mock
    private FileService fileService;

    @InjectMocks
    private EpisodeService episodeService;

    @Test
    void should_CreateEpisodeAndFileMetadata_When_TitleIsUniqueAndNovelExists() {
        // Given
        EpisodeRequest episodeRequest = EpisodeRequest.builder()
                .title(episodeTitle)
                .novelId(novelId)
                .releaseStatus(ReleaseStatus.PUBLIC)
                .build();

        Novel novel = createNovelWithId(novelId, novelTitle, summary, publicationYear);
        Episode episode = createEpisodeWithId(episodeId, episodeRequest.title(), ReleaseStatus.PUBLIC);
        FileMetadata fileMetadata = createFileMetadataWithId(fileMetadataId);

        when(novelRepository.findById(novelId)).thenReturn(Optional.of(novel));
        when(episodeRepository.findByTitleAndNovelId(episodeRequest.title(), episodeRequest.novelId())).thenReturn(Optional.empty());
        when(episodeRepository.findMaxChapterNumberByNovelId(novelId)).thenReturn(1);
        when(episodeRepository.save(any(Episode.class))).thenReturn(episode);
        when(fileMetadataRepository.save(any(FileMetadata.class))).thenReturn(fileMetadata);

        // When
        EpisodeResponse episodeResponse = episodeService.createEpisode(episodeRequest);

        // Then
        assertThat(episodeResponse.episodeId()).isEqualTo(episodeId);
        verify(episodeRepository, times(1)).save(any(Episode.class));
        verify(fileMetadataRepository, times(2)).save(any(FileMetadata.class));
    }

    @Test
    void should_ThrowEpisodeAlreadyExistsException_When_CreatingEpisodeWithDuplicateTitle() {
        // Given
        EpisodeRequest episodeRequest = EpisodeRequest.builder()
                .title(episodeTitleDuplicated)
                .novelId(novelId)
                .releaseStatus(ReleaseStatus.PUBLIC)
                .build();

        Episode episode = createEpisodeWithId(episodeId, episodeRequest.title(), ReleaseStatus.PUBLIC);

        when(episodeRepository.findByTitleAndNovelId(episodeRequest.title(), episodeRequest.novelId())).thenReturn(Optional.of(episode));

        // When
        EpisodeAlreadyExistsException exception = assertThrows(EpisodeAlreadyExistsException.class,
                () -> episodeService.createEpisode(episodeRequest));

        // Then
        assertThat(exception.getMessage()).contains(EPISODE_TITLE_DUPLICATED.getMessage());
    }
}
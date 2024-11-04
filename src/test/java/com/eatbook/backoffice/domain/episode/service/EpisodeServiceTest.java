package com.eatbook.backoffice.domain.episode.service;

import com.eatbook.backoffice.domain.episode.dto.EpisodeRequest;
import com.eatbook.backoffice.domain.episode.dto.EpisodeResponse;
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

import java.lang.reflect.Field;
import java.util.Optional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
    void createEpisodeWithUniqueTitleAndNovelThatExistsShouldCreateEpisodeAndFileMetadata() {
        // Given
        EpisodeRequest episodeRequest = EpisodeRequest.builder()
                .title("Unique Episode Title")
                .novelId("novelId")
                .releaseStatus(ReleaseStatus.PUBLIC)
                .build();

        Novel novel = createNovelWithId("novelId", "Title", "Summary", 2021);
        Episode episode = createEpisodeWithId("episodeId", episodeRequest.title(), ReleaseStatus.PUBLIC);
        FileMetadata fileMetadata = createFileMetadataWithId("fileMetadataId");

        when(novelRepository.findById("novelId")).thenReturn(Optional.of(novel));
        when(episodeRepository.findByTitleAndNovelId(episodeRequest.title(), episodeRequest.novelId())).thenReturn(Optional.empty());
        when(episodeRepository.findMaxChapterNumberByNovelId("novelId")).thenReturn(1);
        when(episodeRepository.save(any(Episode.class))).thenReturn(episode);
        when(fileMetadataRepository.save(any(FileMetadata.class))).thenReturn(fileMetadata);
        when(fileService.getPresignUrl("novelId", EPISODE_CONTENT_TYPE, "episodeId/episode")).thenReturn("presignedUrl");

        // When
        EpisodeResponse episodeResponse = episodeService.createEpisode(episodeRequest);

        // Then
        assertThat(episodeResponse.episodeId()).isEqualTo("episodeId");
        assertThat(episodeResponse.presignedUrl()).isEqualTo("presignedUrl");
        verify(episodeRepository, times(1)).save(any(Episode.class));
        verify(fileMetadataRepository, times(1)).save(any(FileMetadata.class));
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

    // 헬퍼 메서드: 테스트용 Episode ID 설정
    public static Episode createEpisodeWithId(String id, String title, ReleaseStatus releaseStatus) {
        Episode episode = Episode.builder()
                .title(title)
                .releaseStatus(releaseStatus)
                .build();

        // 리플렉션을 사용하여 id 설정
        try {
            Field idField = Episode.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(episode, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return episode;
    }

    // 헬퍼 메서드: FileMetadata ID 설정
    public static FileMetadata createFileMetadataWithId(String id) {
        FileMetadata fileMetadata = FileMetadata.builder().build();

        // 리플렉션을 사용하여 id 설정
        try {
            Field idField = FileMetadata.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(fileMetadata, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return fileMetadata;
    }
}
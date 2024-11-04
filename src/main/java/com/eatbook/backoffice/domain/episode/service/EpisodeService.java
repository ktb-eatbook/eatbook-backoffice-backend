package com.eatbook.backoffice.domain.episode.service;

import com.eatbook.backoffice.domain.episode.dto.EpisodeRequest;
import com.eatbook.backoffice.domain.episode.dto.EpisodeResponse;
import com.eatbook.backoffice.domain.episode.exception.EpisodeAlreadyExistsException;
import com.eatbook.backoffice.domain.episode.repository.EpisodeRepository;
import com.eatbook.backoffice.domain.episode.repository.FileMetadataRepository;
import com.eatbook.backoffice.domain.novel.exception.NovelNotFoundException;
import com.eatbook.backoffice.domain.novel.repository.NovelRepository;
import com.eatbook.backoffice.domain.novel.service.FileService;
import com.eatbook.backoffice.entity.Episode;
import com.eatbook.backoffice.entity.FileMetadata;
import com.eatbook.backoffice.entity.Novel;
import com.eatbook.backoffice.entity.constant.ContentType;
import com.eatbook.backoffice.entity.constant.ReleaseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.eatbook.backoffice.domain.episode.response.EpisodeErrorCode.EPISODE_TITLE_DUPLICATED;
import static com.eatbook.backoffice.domain.novel.response.NovelErrorCode.NOVEL_NOT_FOUND;
import static com.eatbook.backoffice.entity.constant.FileType.SCRIPT;

/**
 * 에피소드를 관리하는 서비스 클래스입니다.
 *
 * @author lavin
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EpisodeService {
    private final EpisodeRepository episodeRepository;
    private final NovelRepository novelRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final FileService fileService;

    private static final ContentType EPISODE_CONTENT_TYPE = ContentType.TXT;
    private static final String EPISODE_DIRECTORY = "episode";

    /**
     * 파일이 업로드될 S3 버킷의 이름입니다.
     */
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    /**
     * AWS S3 버킷의 지역입니다.
     */
    @Value("${cloud.aws.region.static}")
    private String region;

    /**
     * 에피소드를 생성하고, FileMetadata를 업데이트합니다.
     *
     * @param episodeRequest 에피소드 생성 요청
     * @return 에피소드 ID와 파일 경로
     * @throws EpisodeAlreadyExistsException 에피소드 제목이 중복될 경우
     * @throws NovelNotFoundException 소설이 존재하지 않을 경우
     */
    @Transactional
    public EpisodeResponse createEpisode(EpisodeRequest episodeRequest) {
        validateEpisodeUniqueness(episodeRequest);

        Novel novel = findNovelById(episodeRequest.novelId());
        Episode episode = createAndSaveEpisode(episodeRequest, novel);
        createFileMetadata(episode, novel.getId());

        String presignedUrl = generatePresignedUrl(novel.getId(), episode.getId());

        return new EpisodeResponse(episode.getId(), presignedUrl);
    }

    /**
     * 에피소드 제목의 유일성을 검증합니다.
     *
     * @param episodeRequest 에피소드 생성 요청
     * @throws EpisodeAlreadyExistsException 에피소드 제목이 중복될 경우
     */
    private void validateEpisodeUniqueness(EpisodeRequest episodeRequest) {
        if (episodeRepository.findByTitleAndNovelId(episodeRequest.title(), episodeRequest.novelId()).isPresent()) {
            throw new EpisodeAlreadyExistsException(EPISODE_TITLE_DUPLICATED);
        }
    }

    /**
     * ID로 소설을 찾아 반환합니다.
     *
     * @param novelId 소설 ID
     * @return 찾은 소설
     * @throws NovelNotFoundException 소설이 존재하지 않을 경우
     */
    private Novel findNovelById(String novelId) {
        return novelRepository.findById(novelId)
                .orElseThrow(() -> new NovelNotFoundException(NOVEL_NOT_FOUND));
    }

    /**
     * 에피소드를 생성하고, Episode 엔티티를 저장합니다.
     *
     * @param episodeRequest 에피소드 생성 요청
     * @param novel 소설
     * @return 생성된 에피소드
     */
    private Episode createAndSaveEpisode(EpisodeRequest episodeRequest, Novel novel) {
        Integer maxChapterNumber = episodeRepository.findMaxChapterNumberByNovelId(novel.getId());
        int nextChapterNumber = (maxChapterNumber != null ? maxChapterNumber : 0) + 1;

        Episode episode = Episode.builder()
                .title(episodeRequest.title())
                .chapterNumber(nextChapterNumber)
                .scheduledReleaseDate(episodeRequest.scheduledReleaseDate())
                .releasedDate(episodeRequest.releaseStatus() == ReleaseStatus.PUBLIC ? LocalDateTime.now() : episodeRequest.releasedDate())
                .releaseStatus(episodeRequest.releaseStatus())
                .novel(novel)
                .build();

        log.info("새로운 에피소드가 생성됨: {} - 제목: {}", episode.getId(), episode.getTitle());

        return episodeRepository.save(episode);
    }

    /**
     * 에피소드에 대한 FileMetadata를 생성합니다.
     *
     * @param episode 에피소드
     * @return 생성된 FileMetadata
     */
    private FileMetadata createFileMetadata(Episode episode, String novelId) {
        FileMetadata fileMetadata = fileMetadataRepository.save(FileMetadata.builder()
                .type(SCRIPT)
                .episode(episode)
                .path("")
                .build());

        String filePath = "https://" + bucketName + ".s3." + region + ".amazonaws.com/"
                + novelId + "/" + EPISODE_DIRECTORY + "/" + episode.getId();
        fileMetadata.setPath(filePath);

        fileMetadata = fileMetadataRepository.save(fileMetadata);

        return fileMetadata;
    }

    /**
     * 에피소드에 대한 Presigned URL을 생성합니다.
     *
     * @param episodeId 에피소드 ID
     * @param novelId 소설 ID
     * @return 생성된 Presigned URL
     */
    private String generatePresignedUrl(String episodeId, String novelId) {
        return fileService.getPresignUrl(novelId, EPISODE_CONTENT_TYPE, episodeId + "/" + EPISODE_DIRECTORY);
    }
}
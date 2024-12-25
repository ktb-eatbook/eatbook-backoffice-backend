package com.eatbook.backoffice.domain.episode.service;

import com.eatbook.backoffice.domain.episode.dto.EpisodeDetailResponse;
import com.eatbook.backoffice.domain.episode.dto.EpisodeRequest;
import com.eatbook.backoffice.domain.episode.dto.EpisodeResponse;
import com.eatbook.backoffice.domain.episode.exception.EpisodeAlreadyExistsException;
import com.eatbook.backoffice.domain.episode.exception.EpisodeNotFoundException;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

import static com.eatbook.backoffice.domain.episode.response.EpisodeErrorCode.EPISODE_NOT_FOUND;
import static com.eatbook.backoffice.domain.episode.response.EpisodeErrorCode.EPISODE_TITLE_DUPLICATED;
import static com.eatbook.backoffice.domain.novel.response.NovelErrorCode.NOVEL_NOT_FOUND;
import static com.eatbook.backoffice.entity.constant.ContentType.TXT;
import static com.eatbook.backoffice.entity.constant.FileType.SCRIPT;
import static com.eatbook.backoffice.global.utils.PathGenerator.generateRelativePath;
import static com.eatbook.backoffice.global.utils.PathGenerator.getFilePath;

/**
 * 에피소드를 관리하는 서비스 클래스.
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
    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaProducerService kafkaProducerService;

    private static final ContentType EPISODE_CONTENT_TYPE = TXT;

    /**
     * AWS S3 에피소드 디렉토리.
     */
    @Value("${cloud.aws.s3.directory.episode}")
    private String episodeDirectory;

    /**
     * AWS S3 스크립트 디렉토리.
     */
    @Value("${cloud.aws.s3.directory.script}")
    private String scriptDirectory;

    @Value("${cloud.aws.s3.bucket.private}")
    private String privateBucket;

    @Value("${cloud.aws.s3.bucket.public}")
    private String publicBucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    private static final String TOPIC = "speech-generation-requests";


    /**
     * 제공된 요청과 파일로 새 에피소드를 생성합니다.
     *
     * @param episodeRequest 에피소드를 생성하기 위한 요청 정보
     * @param file 에피소드와 연결할 파일
     * @return EpisodeResponse 객체로, 새로 생성된 에피소드 ID와 해당 presigned URL이 포함됩니다.
     * @throws EpisodeAlreadyExistsException 에피소드 제목이 이미 존재할 경우
     * @throws NovelNotFoundException 소설이 존재하지 않을 경우
     */
    @Transactional
    public EpisodeResponse createEpisode(EpisodeRequest episodeRequest, MultipartFile file) {
        checkForDuplicateEpisodeTitle(episodeRequest);

        Novel novel = findNovelById(episodeRequest.novelId());
        Episode episode = createAndSaveEpisode(episodeRequest, novel);
        FileMetadata fileMetadata = createAndSaveFileMetadata(episode, novel.getId());

        String filePath = generateRelativePath(
                novel.getId(),
                episodeDirectory,
                episode.getId(),
                scriptDirectory,
                fileMetadata.getId()
        );

        String presignedURL = fileService.uploadFileToBucket(filePath, file, TXT.getMimeType(), privateBucket);

        //Kafka를 통해 AI 작업 요청 전송
        String taskId = generateTaskId(episode.getId(), novel.getId(), fileMetadata.getId());
        kafkaProducerService.sendMessage(TOPIC, taskId);

        //Redis에 초기 작업 상태 저장
        redisTemplate.opsForValue().set("task:" + taskId + ":status", "PENDING");

        return new EpisodeResponse(episode.getId(), presignedURL);
    }


    private String generateTaskId(String episodeId, String novelId, String fileMetadataId) {
        return String.format("%s-%s-%s", novelId, episodeId, fileMetadataId);
    }

    private String createKafkaMessage(String taskId, String presignedURL) {
        return String.format(
                "{\"taskId\":\"%s\", \"fileUrl\":\"%s\"}",
                taskId,
                presignedURL
        );
    }

    /**
     * 지정된 에피소드 ID를 기반으로 에피소드 정보를 수정합니다.
     * 새로운 파일이 업로드될 경우 기존 파일을 대체합니다.
     *
     * @param episodeId 수정할 에피소드의 ID.
     * @param episodeRequest 수정 요청 데이터.
     * @param file 새로운 파일 (선택 사항).
     * @return 수정된 에피소드의 상세 정보.
     * @throws EpisodeNotFoundException 에피소드가 존재하지 않을 경우 예외 발생.
     */
    @Transactional
    public EpisodeDetailResponse updateEpisode(String episodeId, EpisodeRequest episodeRequest, MultipartFile file) {
        // 에피소드 존재 여부 확인
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new EpisodeNotFoundException(EPISODE_NOT_FOUND));

        // 제목 중복 체크
        episodeRepository.findByTitleAndNovelId(episodeRequest.title(), episode.getNovel().getId())
                .ifPresent(existingEpisode -> {
                    if (!existingEpisode.getId().equals(episodeId)) {
                        throw new EpisodeAlreadyExistsException(EPISODE_TITLE_DUPLICATED);
                    }
                });

        // 필드 업데이트
        episode.setTitle(episodeRequest.title());
        episode.setScheduledReleaseDate(episodeRequest.scheduledReleaseDate());
        episode.setReleasedDate(episodeRequest.releaseStatus() == ReleaseStatus.PUBLIC
                ? LocalDateTime.now()
                : episodeRequest.releasedDate());
        episode.setReleaseStatus(episodeRequest.releaseStatus());

        // 파일 업로드 처리
        if (file != null && !file.isEmpty()) {
            // 기존 파일 메타데이터 가져오기
            FileMetadata existingFileMetadata = fileMetadataRepository.findByEpisodeId(episodeId);

            // 기존 파일 삭제
//            fileService.deleteFile(existingFileMetadata.getPath());

            // 새로운 파일 업로드
            String filePath = generateRelativePath(
                    episode.getNovel().getId(),
                    episodeDirectory,
                    episode.getId(),
                    scriptDirectory,
                    existingFileMetadata.getId()
            );

            String presignedURL = fileService.uploadFileToBucket(filePath, file, TXT.getMimeType(), privateBucket);

            // 파일 메타데이터 갱신
            existingFileMetadata.setPath(filePath);
            fileMetadataRepository.save(existingFileMetadata);

            log.info("새로운 파일이 업로드되었습니다: {}", filePath);
        }

        // 변경 사항 저장
        episode = episodeRepository.save(episode);

        // 수정된 에피소드 상세 정보 반환
        return EpisodeDetailResponse.builder()
                .id(episode.getId())
                .title(episode.getTitle())
                .chapter(episode.getChapterNumber())
                .scheduledDate(episode.getScheduledReleaseDate())
                .releaseDate(episode.getReleasedDate())
                .releaseStatus(episode.getReleaseStatus())
                .novelId(episode.getNovel().getId())
                .viewCount(episode.getViewCount())
                .createdAt(episode.getCreatedAt())
                .updatedAt(episode.getUpdatedAt())
                .build();
    }

    /**
     * 에피소드 ID를 기반으로 상세 정보를 조회합니다.
     *
     * @param episodeId 조회할 에피소드의 ID
     * @return 조회된 에피소드 상세 정보
     * @throws EpisodeNotFoundException 에피소드가 존재하지 않을 경우 예외 발생
     */
    @Transactional(readOnly = true)
    public EpisodeDetailResponse getEpisodeDetails(String episodeId) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new EpisodeNotFoundException(EPISODE_NOT_FOUND));

        return EpisodeDetailResponse.builder()
                .id(episode.getId())
                .title(episode.getTitle())
                .chapter(episode.getChapterNumber())
                .scheduledDate(episode.getScheduledReleaseDate())
                .releaseDate(episode.getReleasedDate())
                .releaseStatus(episode.getReleaseStatus())
                .novelId(episode.getNovel().getId())
                .viewCount(episode.getViewCount())
                .createdAt(episode.getCreatedAt())
                .updatedAt(episode.getUpdatedAt())
                .build();
    }

    /**
     * 에피소드 제목이 중복되는지 확인합니다.
     *
     * @param episodeRequest 에피소드 생성 요청
     * @throws EpisodeAlreadyExistsException 에피소드 제목이 중복될 경우
     */
    private void checkForDuplicateEpisodeTitle(EpisodeRequest episodeRequest) {
        episodeRepository.findByTitleAndNovelId(episodeRequest.title(), episodeRequest.novelId())
                .ifPresent(ep -> { throw new EpisodeAlreadyExistsException(EPISODE_TITLE_DUPLICATED); });
    }

    /**
     * novelId로 소설을 찾아 반환합니다.
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
     * 에피소드를 생성하고, 저장합니다.
     *
     * @param episodeRequest 에피소드 생성 요청
     * @param novel 에피소드가 속한 소설
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

        novel.addEpisode(episode);

        log.info("새로운 에피소드가 생성됨: {} - 제목: {}", episode.getId(), episode.getTitle());

        return episodeRepository.save(episode);
    }

    /**
     * 에피소드에 대한 파일 메타데이터를 생성하고, 저장합니다.
     *
     * @param episode 에피소드
     * @param novelId 에피소드가 속한 소설 ID
     * @return 생성된 파일 메타데이터
     */
    private FileMetadata createAndSaveFileMetadata(Episode episode, String novelId) {

        FileMetadata fileMetadata = fileMetadataRepository.save(FileMetadata.builder()
                .type(SCRIPT)
                .episode(episode)
                .path("")
                .build());

        String filePath = getFilePath(
                privateBucket,
                region,
                novelId,
                episodeDirectory,
                episode.getId(),
                scriptDirectory,
                fileMetadata.getId()
        );

        fileMetadata.setPath(filePath);
        return fileMetadataRepository.save(fileMetadata);
    }

    /**
     * 지정된 에피소드 ID에 해당하는 에피소드 삭제합니다.
     *
     * @param episodeId 삭제할 에피소드의 ID.
     */
    @Transactional
    public void deleteEpisode(String episodeId) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new EpisodeNotFoundException(EPISODE_NOT_FOUND));
        episodeRepository.delete(episode);
    }
}
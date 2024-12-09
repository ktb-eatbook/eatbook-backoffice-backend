package com.eatbook.backoffice.domain.episode.controller;

import com.eatbook.backoffice.domain.episode.dto.EpisodeDetailResponse;
import com.eatbook.backoffice.domain.episode.dto.EpisodeRequest;
import com.eatbook.backoffice.domain.episode.dto.EpisodeResponse;
import com.eatbook.backoffice.domain.episode.response.EpisodeSuccessCode;
import com.eatbook.backoffice.domain.episode.service.EpisodeService;
import com.eatbook.backoffice.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.eatbook.backoffice.domain.episode.response.EpisodeSuccessCode.EPISODE_CREATED;

/**
 * 에피소드를 관리하기 위한 컨트롤러 클래스입니다.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/episode")
public class EpisodeController {
    private final EpisodeService episodeService;

    /**
     * 새로운 에피소드를 생성합니다.
     *
     * @param episodeRequest 에피소드를 생성하기 위한 요청 객체입니다.
     *                      이 객체는 Spring의 {@link Validated} 어노테이션을 사용하여 유효성을 검사해야 합니다.
     *
     * @return {@link HttpStatus#CREATED} 상태 코드를 갖는 ResponseEntity와
     *         성공 코드 {@link EpisodeSuccessCode#EPISODE_CREATED}를 포함하는 ApiResponse입니다.
     *         이 ApiResponse에는 생성된 에피소드Id와 텍스트파일이 업로드될 presigned URL 정보가 포함됩니다.
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createEpisode(
            @Validated @RequestPart("episodeRequest") EpisodeRequest episodeRequest,
            @RequestPart("file") MultipartFile file) {

        log.info("Create Episode Request: {}", episodeRequest);

        EpisodeResponse response = episodeService.createEpisode(episodeRequest, file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.of(EPISODE_CREATED, response));
    }

    /**
     * 에피소드 상세 정보를 조회합니다.
     *
     * @param episodeId 조회할 에피소드의 ID입니다.
     * @return {@link HttpStatus#OK} 상태 코드를 갖는 ResponseEntity와
     *         성공 코드 {@link EpisodeSuccessCode#EPISODE_FETCHED}를 포함하는 ApiResponse입니다.
     *         이 ApiResponse에는 조회된 에피소드의 상세 정보가 포함됩니다.
     */
    @GetMapping("/{episodeId}")
    public ResponseEntity<ApiResponse> getEpisodeDetails(@PathVariable String episodeId) {

        log.info("Get Episode Details Request: episodeId={}", episodeId);

        EpisodeDetailResponse response = episodeService.getEpisodeDetails(episodeId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.of(EpisodeSuccessCode.EPISODE_FETCHED, response));
    }
}
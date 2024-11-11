package com.eatbook.backoffice.domain.novel.controller;

import com.eatbook.backoffice.domain.novel.dto.NovelListResponse;
import com.eatbook.backoffice.domain.novel.dto.NovelRequest;
import com.eatbook.backoffice.domain.novel.dto.NovelResponse;
import com.eatbook.backoffice.domain.novel.response.NovelSuccessCode;
import com.eatbook.backoffice.domain.novel.service.NovelService;
import com.eatbook.backoffice.global.response.ApiResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.eatbook.backoffice.domain.novel.response.NovelSuccessCode.GET_NOVEL_LIST;
import static com.eatbook.backoffice.domain.novel.response.NovelSuccessCode.NOVEL_CREATED;

/**
 * 소설 관련 작업을 관리하는 컨트롤러.
 *
 * @author lavin
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin")
public class NovelController {
    private final NovelService novelService;

    /**
     * 새로운 소설을 생성합니다.
     *
     * @param novelRequest 소설 세부 정보가 포함된 요청 객체. 이 객체는 Spring의 {@link Validated} 어노테이션을 사용하여 유효성을 검사해야 합니다.
     * @return {@link HttpStatus#CREATED} 상태 코드를 갖는 ResponseEntity와
     * 성공 코드 {@link NovelSuccessCode#NOVEL_CREATED}를 포함하는 ApiResponse입니다.
     * 이 ApiResponse에는 생성된 소설Id와 커버 이미지용 presigned URL 정보가 포함됩니다.
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createNovel(@Validated @RequestBody NovelRequest novelRequest) {

        log.info("Create Novel Request: {}", novelRequest);

        NovelResponse response = novelService.createNovel(novelRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.of(NOVEL_CREATED, response));
    }
    
    /**
     * 소설 목록을 조회합니다.
     *
     * @param page 조회할 페이지 번호. 1 이상의 값이어야 합니다.
     * @param size 페이지당 표시할 소설 수. 1 이상의 값이어야 합니다.
     * @return {@link HttpStatus#OK} 상태 코드를 갖는 ResponseEntity와
     * 성공 코드 {@link NovelSuccessCode#GET_NOVEL_LIST}를 포함하는 ApiResponse입니다.
     * 이 ApiResponse에는 조회된 소설 목록이 포함됩니다.
     */
    @GetMapping("/novels")
    public ResponseEntity<ApiResponse> getNovelList(@RequestParam(name = "page") @Min(1) final int page,
                                                    @RequestParam(name = "size") @Min(1) final int size) {

        NovelListResponse novelList = novelService.getNovelList(page, size);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.of(GET_NOVEL_LIST, novelList));
    }
}
package com.eatbook.backoffice.domain.novel.response;

import com.eatbook.backoffice.global.response.StatusCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum NovelErrorCode implements StatusCode {
    NOVEL_ALREADY_EXISTS("NOVEL_ALREADY_EXISTS", "이미 존재하는 소설입니다. 이미 같은 제목과 작가를 가진 소설이 존재합니다."),
    NOVEL_NOT_FOUND("NOVEL_NOT_FOUND", "존재하지 않는 소설입니다."),
    PAGE_OUT_OF_BOUNDS("PAGE_OUT_OF_BOUNDS", "요청된 페이지가 총 페이지 수를 초과했습니다."),
    PRESIGNED_URL_GENERATION_FAILED("PRESIGNED_URL_GENERATION_FAILED", "presinged url 생성에 실패했습니다. 에러메세지: %s"),
    S3_PRE_SIGNED_URL_GENERATION_FAILED("S3_PRE_SIGNED_URL_GENERATION_FAILED", "S3 presinged url 생성에 실패했습니다."),
    ;

    private final String code;
    private final String message;
}

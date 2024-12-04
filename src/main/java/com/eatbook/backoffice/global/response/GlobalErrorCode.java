package com.eatbook.backoffice.global.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum GlobalErrorCode implements StatusCode {
    METHOD_NOT_ALLOWED("METHOD_NOT_ALLOWED", "허용되지 않은 메소드입니다."),
    FAILED_TO_AUTH("FAILED_TO_AUTH", "인증에 실패하였습니다."),
    API_NOT_FOUND("API_NOT_FOUND", "요청하신 API를 찾을 수 없습니다."),
    VALIDATION_ERROR("VALIDATION_ERROR", "요청한 데이터가 유효하지 않습니다."),
    PAGE_OUT_OF_BOUNDS("PAGE_OUT_OF_BOUNDS", "요청된 페이지가 총 페이지 수를 초과했습니다."),
    UNHANDLED_EXCEPTION("UNHANDLED_EXCEPTION", "처리되지 않은 예외가 발생하였습니다."),
    ;

    private final String code;
    private final String message;

}




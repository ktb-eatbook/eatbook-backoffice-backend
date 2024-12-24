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
    NOT_EXIST_USER("NOT_EXIST_USER", "존재하지 않는 유저입니다."),
    USER_PASSWORD_NOT_MATCHED("USER_PASSWORD_NOT_MATCHED", "비밀번호가 틀렸습니다."),
    JWT_MALFORMED("JWT_MALFORMED", "JWT 토큰이 올바르지 않습니다."),
    JWT_UNSUPPORTED("JWT_UNSUPPORTED", "지원되지 않는 JWT 토큰입니다."),
    JWT_EXPIRED("JWT_EXPIRED", "JWT 토큰이 만료되었습니다."),
    JWT_MISSING_CLAIM("JWT_MISSING_CLAIM", "필수 JWT 클레임이 누락되었습니다."),
    JWT_INCORRECT_CLAIM("JWT_INCORRECT_CLAIM", "JWT 클레임 값이 잘못되었습니다."),
    JWT_INVALID_SIGNATURE("JWT_INVALID_SIGNATURE", "JWT 서명이 유효하지 않습니다."),
    JWT_INVALID_CLAIMS("JWT_INVALID_CLAIMS", "JWT 클레임이 유효하지 않습니다."),
    JWT_INVALID_FORMAT("JWT_INVALID_FORMAT", "JWT 토큰의 형식이 유효하지 않습니다."),
    JWT_UNKNOWN_ERROR("JWT_UNKNOWN_ERROR", "알 수 없는 JWT 토큰 오류가 발생했습니다."),
    SECURITY_ROLE_NOT_FOUND("SECURITY_ROLE_NOT_FOUND", "해당하는 권한을 찾을 수 없습니다."),
    INVALID_ROLE("INVALID_ROLE", "유효하지 않은 권한입니다."),
    NO_SUCH_TASK("NO_SUCH_TASK", "해당하는 작업을 찾을 수 없습니다."),
    ;

    private final String code;
    private final String message;

}




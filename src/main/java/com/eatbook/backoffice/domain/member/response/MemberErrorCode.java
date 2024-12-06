package com.eatbook.backoffice.domain.member.response;

import com.eatbook.backoffice.global.response.StatusCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum MemberErrorCode implements StatusCode {
    MEMBER_ALREADY_EXISTS("MEMBER_ALREADY_EXISTS", "이미 존재하는 회원입니다. 이미 같은 이메일을 가진 회원이 존재합니다."),
    MEMBER_NOT_FOUND("MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다."),
    MEMBER_IS_NOT_ADMIN("MEMBER_IS_NOT_ADMIN", "관리자가 아닙니다."),
    INVALID_ROLE("INVALID_ROLE", "유효하지 않은 Role 입니다."),
    REFRESH_TOKEN_IS_NOT_VALID("REFRESH_TOKEN_IS_NOT_VALID", "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_IS_MISSING("REFRESH_TOKEN_IS_MISSING", "리프레시 토큰이 없습니다."),
    INVALID_AGE_GROUP("INVALID_AGE_GROUP", "유효하지 않은 연령대입니다."),
    ;

    private final String code;
    private final String message;
}

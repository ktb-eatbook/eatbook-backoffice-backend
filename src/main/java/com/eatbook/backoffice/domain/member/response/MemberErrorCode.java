package com.eatbook.backoffice.domain.member.response;

import com.eatbook.backoffice.global.response.StatusCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum MemberErrorCode implements StatusCode {
    MEMBER_ALREADY_EXISTS("MEMBER_ALREADY_EXISTS", "이미 존재하는 회원입니다. 이미 같은 이메일을 가진 회원이 존재합니다."),
    ;

    private final String code;
    private final String message;
}

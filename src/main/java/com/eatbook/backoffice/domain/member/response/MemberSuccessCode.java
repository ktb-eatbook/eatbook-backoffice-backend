package com.eatbook.backoffice.domain.member.response;

import com.eatbook.backoffice.global.response.StatusCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum MemberSuccessCode implements StatusCode {
    MEMBER_CREATED("MEMBER_CREATED", "회원이 성공적으로 생성되었습니다."),
    GET_MEMBER_LIST("GET_MEMBER_LIST", "회원 목록을 성공적으로 조회했습니다."),
    GET_MEMBER_DETAIL("GET_MEMBER_DETAIL", "회원 상세 정보를 성공적으로 조회했습니다."),
    UPDATE_MEMBER("UPDATE_MEMBER", "회원 정보를 성공적으로 수정했습니다."),
    UPDATE_MEMBER_ROLE("UPDATE_MEMBER_ROLE", "회원 권한을 성공적으로 수정했습니다."),
    DELETE_MEMBER("DELETE_MEMBER", "회원을 성공적으로 삭제했습니다.")
    ;

    private final String code;
    private final String message;
}

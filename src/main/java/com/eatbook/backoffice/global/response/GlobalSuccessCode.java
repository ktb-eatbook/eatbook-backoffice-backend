package com.eatbook.backoffice.global.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum GlobalSuccessCode implements StatusCode {
    API_SUCCESS("API_SUCCESS", "요청이 성공적으로 처리되었습니다."),
    LOGIN_SUCCESS("LOGIN_SUCCESS", "로그인에 성공했습니다."),
    LOGOUT_SUCCESS("LOGOUT_SUCCESS", "로그아웃에 성공했습니다."),
    JOB_PENDING("JOB_PENDING", "작업이 대기 중입니다."),
    JOB_START("JOB_START", "작업이 시작되었습니다."),
    JOB_SUCCESS("JOB_SUCCESS", "작업이 성공적으로 처리되었습니다."),
    EMAIL_VERIFICATION_SENT("EMAIL_VERIFICATION_SENT", "이메일 인증 코드가 전송되었습니다."),
    EMAIL_VERIFICATION_SUCCESS("EMAIL_VERIFICATION_SUCCESS", "이메일 인증이 성공했습니다."),
    EMAIL_VERIFICATION_FAILED("EMAIL_VERIFICATION_FAILED", "이메일 인증이 실패했습니다."),
    USER_ROLE_FETCH_SUCCESS("USER_ROLE_FETCH_SUCCESS", "사용자의 권한을 성공적으로 가져왔습니다."),
    ;

    private final String code;
    private final String message;
}

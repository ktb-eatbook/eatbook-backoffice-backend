package com.eatbook.backoffice.domain.member.dto;

import jakarta.validation.constraints.NotBlank;

public record SignUpRequest(
        @NotBlank(message = "이메일은 필수 입력 항목입니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        String password,

        @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
        String nickName,

        @NotBlank(message = "성별은 필수 입력 항목입니다.")
        String gender,

        @NotBlank(message = "연령대는 필수 입력 항목입니다.")
        String ageGroup
) {
}
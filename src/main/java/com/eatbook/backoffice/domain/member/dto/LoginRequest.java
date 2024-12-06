package com.eatbook.backoffice.domain.member.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "email 필수 입력 값입니다.") String email,
        @NotBlank(message = "password 필수 입력 값입니다.") String password
) {
}
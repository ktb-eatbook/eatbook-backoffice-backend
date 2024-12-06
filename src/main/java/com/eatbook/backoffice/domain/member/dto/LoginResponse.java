package com.eatbook.backoffice.domain.member.dto;

import com.eatbook.backoffice.entity.constant.Role;

public record LoginResponse (
    String accessToken,
    String refreshToken,
    Role role
) {
    public static LoginResponse of(
        final String accessToken,
        final String refreshToken,
        final Role role
    ) {
        return new LoginResponse(accessToken, refreshToken, role);
    }
}
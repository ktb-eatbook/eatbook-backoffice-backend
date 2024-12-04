package com.eatbook.backoffice.domain.member.dto;

import com.eatbook.backoffice.entity.Member;
import com.eatbook.backoffice.entity.constant.Role;

public record MemberRequest(
        String nickname,
        String email,
        String password,
        String role,
        String profileImageUrl
) {
    public static MemberRequest of(
            final String nickname,
            final String email,
            final String password,
            final String role,
            final String profileImageUrl
    ) {
        return new MemberRequest(nickname, email, password, role, profileImageUrl);
    }

    public Member toEntity() {
        return Member.builder()
               .nickname(nickname)
               .email(email)
               .passwordHash(password)
               .role(Role.valueOf(role))
               .profileImageUrl(profileImageUrl)
               .build();
    }
}

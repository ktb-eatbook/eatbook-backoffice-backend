package com.eatbook.backoffice.security.auth.jwt;

import com.eatbook.backoffice.domain.member.repository.MemberRepository;
import com.eatbook.backoffice.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;

import static com.eatbook.backoffice.global.response.GlobalErrorCode.NOT_EXIST_USER;
import static com.eatbook.backoffice.global.response.GlobalErrorCode.USER_PASSWORD_NOT_MATCHED;

@Component
@RequiredArgsConstructor
public class CustomPasswordAuthenticationManager implements AuthenticationProvider {
    private final MemberRepository memberRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String principal = (String) authentication.getPrincipal();

        Member member = memberRepository.findById(principal)
                .orElseThrow(() -> new BadCredentialsException(NOT_EXIST_USER.getMessage()));

        if (!authentication.getCredentials().equals(member.getPasswordHash())) {
            throw new BadCredentialsException(USER_PASSWORD_NOT_MATCHED.getMessage());
        }
        CustomPasswordAuthenticationToken token = new CustomPasswordAuthenticationToken(
                member.getId(), member.getPasswordHash(),
                Collections.singleton(new SimpleGrantedAuthority(member.getRole().toString()))
        );
        token.setId(member.getId());
        token.setName(member.getNickname());
        token.setRole(String.valueOf(member.getRole()));
        return token;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(CustomPasswordAuthenticationToken.class);
    }
}

package com.eatbook.backoffice.domain.member.service;

import com.eatbook.backoffice.domain.member.dto.LoginRequest;
import com.eatbook.backoffice.domain.member.dto.LoginResponse;
import com.eatbook.backoffice.domain.member.dto.SignUpRequest;
import com.eatbook.backoffice.domain.member.exception.MemberAuthenticationException;
import com.eatbook.backoffice.domain.member.repository.MemberRepository;
import com.eatbook.backoffice.domain.novel.service.FileService;
import com.eatbook.backoffice.entity.Member;
import com.eatbook.backoffice.entity.constant.AgeGroup;
import com.eatbook.backoffice.entity.constant.Gender;
import com.eatbook.backoffice.entity.constant.Role;
import com.eatbook.backoffice.security.auth.jwt.JwtAuthToken;
import com.eatbook.backoffice.security.auth.jwt.JwtAuthTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import static com.eatbook.backoffice.domain.member.response.MemberErrorCode.MEMBER_ALREADY_EXISTS;
import static com.eatbook.backoffice.entity.constant.ContentType.JPEG;
import static com.eatbook.backoffice.global.response.GlobalErrorCode.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberAuthService {
    private final MemberRepository memberRepository;
    private final FileService fileService;
    private final JwtAuthTokenProvider tokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;
    @Lazy
    private final AuthenticationManager authenticationManager;

    private static final String PROFILE_IMAGE_PATH = "profile-images/";

    @Transactional
    public void signUp(SignUpRequest dto, MultipartFile profileImage) throws MemberAuthenticationException {
        validateEmailDuplication(dto.email());

        String hashedPassword = passwordEncoder.encode(dto.password());
        String profileImageUrl = null;

        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                byte[] fileBytes = profileImage.getBytes();
                String objectKey = PROFILE_IMAGE_PATH + dto.email();
                profileImageUrl = fileService.uploadProfileImage(objectKey, fileBytes, JPEG);
            } catch (IOException e) {
                throw new RuntimeException("프로필 이미지 업로드 중 오류 발생", e);
            }
        }

        Member newUser = Member.builder()
                .passwordHash(hashedPassword)
                .nickname(dto.nickName())
                .email(dto.email())
                .ageGroup(AgeGroup.valueOf(dto.ageGroup()))
                .role(Role.PENDING_ADMIN)
                .gender(Gender.valueOf(dto.gender()))
                .profileImageUrl(profileImageUrl)
                .build();
        memberRepository.save(newUser);
    }

    @Transactional
    public LoginResponse login(LoginRequest dto) throws AuthenticationException {
        Member member = memberRepository.findMemberByEmail(dto.email())
                .orElseThrow(() -> new MemberAuthenticationException(NOT_EXIST_USER));

        if (!passwordEncoder.matches(dto.password(), member.getPasswordHash())) {
            throw new MemberAuthenticationException(USER_PASSWORD_NOT_MATCHED);
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
        );

        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findAny().orElseThrow(() -> new BadCredentialsException(SECURITY_ROLE_NOT_FOUND.getMessage()));

        String accessToken = createAccessToken(member.getId(), role);
        String refreshToken = createRefreshToken(member.getId());

        updateLastLogin(member);

        return LoginResponse.of(accessToken, refreshToken, Role.valueOf(role));
    }

    private void validateEmailDuplication(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new MemberAuthenticationException(MEMBER_ALREADY_EXISTS);
        }
    }

    private String createAccessToken(String userId, String role) {
        Map<String, String> claims = Map.of("id", userId, "role", role);
        JwtAuthToken jwtAuthToken = tokenProvider.createAuthToken(userId, role, claims);
        return jwtAuthToken.getToken();
    }

    private String createRefreshToken(String userId) {
        JwtAuthToken jwtAuthToken = tokenProvider.createRefreshToken(userId);
        return jwtAuthToken.getToken();
    }

    protected void updateLastLogin(Member member) {
        member.setLastLogin(LocalDateTime.now());
        memberRepository.save(member);
    }
}
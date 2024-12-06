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

/**
 * 이 클래스는 회원 인증과 관련된 서비스를 제공하며, 가입 및 로그인과 같은 기능을 수행합니다.
 * 멤버 저장소, 파일 서비스, JWT 토큰 공급자, 비밀번호 인코더와 상호 작용합니다.
 *
 * @author lavin
 * @since 1.0.0
 */
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

    /**
     * 제공된 가입 요청과 프로필 이미지를 사용하여 새로운 멤버를 등록합니다.
     *
     * @param dto 가입 요청 정보를 포함하는 DTO.
     * @param profileImage 업로드할 프로필 이미지 파일.
     * @throws MemberAuthenticationException 이메일이 이미 데이터베이스에 존재할 경우 발생.
     */
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

    /**
     * 로그인 요청을 처리하고, 유효한 경우 JWT 토큰을 발행합니다.
     *
     * @param dto 로그인 요청 정보를 포함하는 DTO.
     * @return 로그인 성공 시 로그인 응답 DTO.
     * @throws AuthenticationException 인증 실패 시 발생.
     */
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

    /**
     * 이메일 중복을 확인합니다.
     *
     * @param email 확인할 이메일.
     * @throws MemberAuthenticationException 이메일이 이미 존재할 경우 발생.
     */
    private void validateEmailDuplication(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new MemberAuthenticationException(MEMBER_ALREADY_EXISTS);
        }
    }

    /**
     * JWT 토큰을 생성합니다.
     *
     * @param userId 사용자 ID.
     * @param role 사용자 역할.
     * @return 생성된 JWT 토큰.
     */
    private String createAccessToken(String userId, String role) {
        Map<String, String> claims = Map.of("id", userId, "role", role);
        JwtAuthToken jwtAuthToken = tokenProvider.createAuthToken(userId, role, claims);
        return jwtAuthToken.getToken();
    }

    /**
     * JWT 갱신 토큰을 생성합니다.
     *
     * @param userId 사용자 ID.
     * @return 생성된 JWT 갱신 토큰.
     */
    private String createRefreshToken(String userId) {
        JwtAuthToken jwtAuthToken = tokenProvider.createRefreshToken(userId);
        return jwtAuthToken.getToken();
    }

    /**
     * 멤버의 마지막 로그인 시간을 업데이트합니다.
     *
     * @param member 업데이트할 멤버.
     */
    protected void updateLastLogin(Member member) {
        member.setLastLogin(LocalDateTime.now());
        memberRepository.save(member);
    }
}
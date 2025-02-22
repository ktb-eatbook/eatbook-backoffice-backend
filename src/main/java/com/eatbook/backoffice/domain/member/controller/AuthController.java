package com.eatbook.backoffice.domain.member.controller;

import com.eatbook.backoffice.domain.member.dto.LoginRequest;
import com.eatbook.backoffice.domain.member.dto.LoginResponse;
import com.eatbook.backoffice.domain.member.dto.SignUpRequest;
import com.eatbook.backoffice.domain.member.service.MemberAuthService;
import com.eatbook.backoffice.domain.member.service.EmailVerificationService;
import com.eatbook.backoffice.global.response.ApiResponse;
import com.eatbook.backoffice.security.auth.service.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.eatbook.backoffice.domain.member.response.MemberSuccessCode.MEMBER_CREATED;
import static com.eatbook.backoffice.global.response.GlobalErrorCode.JWT_MALFORMED;
import static com.eatbook.backoffice.global.response.GlobalSuccessCode.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final MemberAuthService memberAuthService;
    private final CookieService cookieService;
    private final EmailVerificationService emailVerificationService; // 추가

    private static final String AUTH_HEADER = "Authorization";
    private static final String AUTH_PREFIX = "Bearer ";
    private static final String REFRESH_TOKEN_COOKIE = "Refresh-Token";

    @PostMapping("/signUp")
    public ResponseEntity<ApiResponse> signUp(
            HttpServletRequest request,
            @RequestPart("data") @Valid SignUpRequest signUpRequest,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        memberAuthService.signUp(signUpRequest, profileImage);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(MEMBER_CREATED));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        LoginResponse loginResponse = memberAuthService.login(loginRequest);

        cookieService.addCookie(response, REFRESH_TOKEN_COOKIE, loginResponse.refreshToken());

        return ResponseEntity.status(HttpStatus.OK)
                .header(AUTH_HEADER, AUTH_PREFIX + loginResponse.accessToken())
                .body(ApiResponse.of(LOGIN_SUCCESS));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletResponse response) {
        cookieService.deleteCookie(response, REFRESH_TOKEN_COOKIE);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(LOGOUT_SUCCESS));
    }

    // ------------------------
    // 이메일 인증 관련 Endpoint
    // ------------------------

    /**
     * 클라이언트가 입력한 이메일로 인증번호를 전송합니다.
     * URL 예시: POST /auth/email/send?email=test@example.com
     */
    @PostMapping("/email/send")
    public ResponseEntity<ApiResponse> sendVerificationEmail(@RequestParam String email) {
        emailVerificationService.sendVerificationCode(email);
        // 전송 성공에 대한 응답 코드를 반환합니다.
        // 예: EMAIL_VERIFICATION_SENT (글로벌 성공 코드에 추가)
        return ResponseEntity.ok(ApiResponse.of(EMAIL_VERIFICATION_SENT));
    }

    /**
     * 클라이언트가 받은 인증번호와 이메일을 제출하여 인증을 검증합니다.
     * URL 예시: POST /auth/email/verify?email=test@example.com&code=123456
     */
    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestParam String email, @RequestParam String code) {
        boolean isVerified = emailVerificationService.verifyCode(email, code);
        if (isVerified) {
            // 이메일 인증 성공 응답 (예: EMAIL_VERIFICATION_SUCCESS)
            return ResponseEntity.ok(ApiResponse.of(EMAIL_VERIFICATION_SUCCESS));
        } else {
            // 이메일 인증 실패 응답 (예: EMAIL_VERIFICATION_FAILED)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(EMAIL_VERIFICATION_FAILED));
        }
    }
    /**
     * 현재 로그인한 사용자의 role 반환
     */
//    @GetMapping("/me")
//    public ResponseEntity<ApiResponse> getUserRole(HttpServletRequest request) {
//        // 헤더에서 JWT 토큰 가져오기
//        String authorizationHeader = request.getHeader(AUTH_HEADER);
//        if (authorizationHeader == null || !authorizationHeader.startsWith(AUTH_PREFIX)) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(ApiResponse.of(JWT_MALFORMED));
//        }
//
//        String token = authorizationHeader.substring(AUTH_PREFIX.length());
//
//        try {
//            // JWT에서 role 추출
//            String role = memberAuthService.getUserRoleFromToken(token);
//            return ResponseEntity.ok(ApiResponse.of(USER_ROLE_FETCH_SUCCESS, role));
//        } catch (Exception e) {
//            log.error("JWT 파싱 실패: {}", e.getMessage());
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(ApiResponse.of(JWT_MALFORMED));
//        }
//    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getUserRole(HttpServletRequest request) {
        String role = null;
        String authorizationHeader = request.getHeader(AUTH_HEADER);

        // access token이 있을 경우 시도
        if (authorizationHeader != null && authorizationHeader.startsWith(AUTH_PREFIX)) {
            String accessToken = authorizationHeader.substring(AUTH_PREFIX.length());
            try {
                role = memberAuthService.getUserRoleFromToken(accessToken);
            } catch (Exception e) {
                log.error("Access token 검증 실패: {}", e.getMessage());
            }
        }

        // access token으로 role 추출에 실패하면 쿠키의 refresh token 사용
        if (role == null) {
            String refreshToken = cookieService.getCookieValue(request, REFRESH_TOKEN_COOKIE);
            if (refreshToken != null) {
                try {
                    role = memberAuthService.getUserRoleFromRefreshToken(refreshToken);
                } catch (Exception e) {
                    log.error("Refresh token 검증 실패: {}", e.getMessage());
                }
            }
        }

        if (role != null) {
            return ResponseEntity.ok(ApiResponse.of(USER_ROLE_FETCH_SUCCESS, role));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(JWT_MALFORMED));
        }
    }
}
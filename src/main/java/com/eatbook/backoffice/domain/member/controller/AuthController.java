package com.eatbook.backoffice.domain.member.controller;

import com.eatbook.backoffice.domain.member.dto.LoginRequest;
import com.eatbook.backoffice.domain.member.dto.LoginResponse;
import com.eatbook.backoffice.domain.member.dto.SignUpRequest;
import com.eatbook.backoffice.domain.member.service.MemberAuthService;
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
import static com.eatbook.backoffice.global.response.GlobalSuccessCode.LOGIN_SUCCESS;
import static com.eatbook.backoffice.global.response.GlobalSuccessCode.LOGOUT_SUCCESS;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final MemberAuthService memberAuthService;
    private final CookieService cookieService;

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
}
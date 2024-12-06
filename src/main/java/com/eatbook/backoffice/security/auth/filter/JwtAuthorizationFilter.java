package com.eatbook.backoffice.security.auth.filter;

import com.eatbook.backoffice.security.auth.jwt.JwtAuthToken;
import com.eatbook.backoffice.security.auth.jwt.JwtAuthTokenProvider;
import com.eatbook.backoffice.security.error.exception.JwtTokenException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    @Value("${jwt.header}")
    private String authHeaderName;

    @Value("${jwt.prefix}")
    private String prefix;

    @Value("${jwt.refresh-token.cookie-name}")
    private String refreshTokenCookieName;

    @Value("${jwt.refresh-token.renewal-threshold}")
    private int renewalThresholdInDays;

    private final JwtAuthTokenProvider tokenProvider;

    private final int renewalRatio = 10;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Optional<String> accessToken = resolveTokenFromHeader(request);
        Optional<String> refreshToken = resolveTokenFromCookie(request);

        // Access Token 검증
        if (accessToken.isPresent()) {
            log.info("Access Token을 검증 중...");
            JwtAuthToken authToken = tokenProvider.convertAuthToken(accessToken.get());

            if (validateAccessToken(authToken)) {
                setAuthentication(accessToken.get());
                filterChain.doFilter(request, response);
                return;
            }
        }

        // Refresh Token 검증 및 Access Token 갱신
        if (refreshToken.isPresent()) {
            log.info("Refresh Token을 검증 중...");
            JwtAuthToken refreshAuthToken = tokenProvider.convertAuthToken(refreshToken.get());
            Claims expiredClaims = tokenProvider.getClaimsFromExpiredToken(accessToken.get());
            String userId = expiredClaims.getSubject();
            String role = expiredClaims.get(JwtAuthToken.AUTHORITIES_KEY, String.class);

            if (validateRefreshTokenAndRenew(response, refreshAuthToken, userId, role)) {
                filterChain.doFilter(request, response);
                return;
            }
            log.warn("Refresh Token이 유효하지 않습니다.");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 Refresh Token입니다.");
            return;
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증 정보가 없습니다.");
    }

    private boolean validateAccessToken(JwtAuthToken authToken) {
        try {
            authToken.getData();
            log.info("Access Token 검증 성공");
            return true;
        } catch (JwtTokenException e) {
            log.error("Access Token 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    private boolean validateRefreshTokenAndRenew(HttpServletResponse response, JwtAuthToken refreshAuthToken, String userId, String role) {
        try {
            Claims refreshClaims = refreshAuthToken.getData();
            log.info("Refresh Token이 유효합니다. 새로운 Access Token을 발급합니다.");

            Map<String, String> claimsMap = Map.of(
                    "id", userId,
                    JwtAuthToken.AUTHORITIES_KEY, role
            );

            String newAccessToken = tokenProvider.createAuthToken(userId, role, claimsMap).getToken();
            response.setHeader(authHeaderName, prefix + " " + newAccessToken);

            // Refresh Token 갱신 필요 시 발급
            if (shouldRenewRefreshToken(refreshAuthToken)) {
                String newRefreshToken = tokenProvider.createRefreshToken(userId).getToken();
                Cookie newRefreshCookie = new Cookie(refreshTokenCookieName, newRefreshToken);
                newRefreshCookie.setHttpOnly(true);
                newRefreshCookie.setPath("/");
                newRefreshCookie.setMaxAge(tokenProvider.getRefreshTokenExpiration());
                response.addCookie(newRefreshCookie);
                log.info("새로운 Refresh Token이 발급되었습니다.");
            }

            setAuthentication(newAccessToken);
            return true;
        } catch (JwtTokenException e) {
            return false;
        }
    }

    private boolean shouldRenewRefreshToken(JwtAuthToken refreshAuthToken) {
        Claims claims = refreshAuthToken.getData();
        Date expiration = claims.getExpiration();
        long remainingTime = expiration.getTime() - System.currentTimeMillis();
        long totalTime = expiration.getTime() - claims.getIssuedAt().getTime();
        long threshold = totalTime / renewalRatio;
        return remainingTime < threshold;
    }

    private Optional<String> resolveTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader(authHeaderName);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(prefix + " ")) {
            return Optional.of(authHeader.substring((prefix + " ").length()));
        }
        return Optional.empty();
    }

    private Optional<String> resolveTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> refreshTokenCookieName.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue);
    }

    private void setAuthentication(String token) {
        try {
            JwtAuthToken jwtAuthToken = tokenProvider.convertAuthToken(token);
            Authentication authentication = tokenProvider.getAuthentication(jwtAuthToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            log.error("Authentication 설정 실패: {}", e.getMessage());
        }
    }
}
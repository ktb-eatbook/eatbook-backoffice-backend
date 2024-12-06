package com.eatbook.backoffice.security.auth.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    @Value("${jwt.refresh-token.expiration}")
    private int refreshTokenExpiration;

    /**
     * 쿠키를 생성하여 응답에 추가합니다.
     */
    public void addCookie(HttpServletResponse response, String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // HTTPS 환경에서만 사용
        cookie.setPath("/");
        cookie.setMaxAge(refreshTokenExpiration);
        response.addCookie(cookie);
    }

    /**
     * 쿠키를 삭제합니다.
     */
    public void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
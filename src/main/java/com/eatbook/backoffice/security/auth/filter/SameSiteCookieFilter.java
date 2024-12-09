package com.eatbook.backoffice.security.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SameSiteCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        filterChain.doFilter(request, response);
        boolean isSecureRequest = request.isSecure();

        response.getHeaders("Set-Cookie").forEach(cookie -> {
            if (!cookie.contains("SameSite") || (isSecureRequest && !cookie.contains("Secure"))) {
                StringBuilder updatedCookie = new StringBuilder(cookie);

                if (!cookie.contains("SameSite")) {
                    updatedCookie.append("; SameSite=None");
                }

                if (isSecureRequest && !cookie.contains("Secure")) {
                    updatedCookie.append("; Secure");
                }

                response.setHeader("Set-Cookie", updatedCookie.toString());
            }
        });
    }
}
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

        if (response.containsHeader("Set-Cookie")) {
            String header = response.getHeader("Set-Cookie");
            if (header != null && !header.contains("SameSite")) {
                response.setHeader("Set-Cookie", header + "; SameSite=None; Secure");
            }
        }
    }
}
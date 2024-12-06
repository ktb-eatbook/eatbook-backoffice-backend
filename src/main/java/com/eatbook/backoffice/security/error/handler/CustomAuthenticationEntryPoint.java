package com.eatbook.backoffice.security.error.handler;

import com.eatbook.backoffice.global.response.ApiResponse;
import com.eatbook.backoffice.global.response.GlobalErrorCode;
import com.eatbook.backoffice.global.response.StatusCode;
import com.eatbook.backoffice.security.error.exception.JwtTokenException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        StatusCode errorCode = GlobalErrorCode.JWT_EXPIRED;

        if (authException instanceof JwtTokenException) {
            errorCode = ((JwtTokenException) authException).getErrorCode();
        }

        log.error("Authentication 예외 발생: {}", authException.getMessage());

        ApiResponse apiResponse = ApiResponse.of(errorCode);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(jsonResponse);
    }
}
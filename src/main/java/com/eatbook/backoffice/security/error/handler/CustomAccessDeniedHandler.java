package com.eatbook.backoffice.security.error.handler;

import com.eatbook.backoffice.global.response.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static com.eatbook.backoffice.global.response.GlobalErrorCode.INVALID_ROLE;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {

        String jsonLog = getJsonLog(request, accessDeniedException);
        log.error(jsonLog);

        ApiResponse apiResponse = ApiResponse.of(INVALID_ROLE);

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(jsonResponse);
    }

    private String getJsonLog(HttpServletRequest request, AccessDeniedException accessDeniedException) throws JsonProcessingException {
        String method = request.getMethod();
        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();
        String clientIP = request.getRemoteAddr();

        Map<String, String> headers = extractHeaders(request);
        Map<String, String[]> parameters = request.getParameterMap();

        Map<String, Object> logData = new HashMap<>();
        logData.put("event", "AccessDenied");
        logData.put("method", method);
        logData.put("url", requestURI + (queryString != null ? "?" + queryString : ""));
        logData.put("clientIP", clientIP);
        logData.put("headers", headers);
        logData.put("parameters", parameters);
        logData.put("message", "권한이 없는 사용자의 요청입니다.");
        logData.put("error", accessDeniedException.getMessage());

        String jsonLog = objectMapper.writeValueAsString(logData);
        return jsonLog;
    }

    /**
     * HttpServletRequest에서 모든 헤더를 추출하여 Map으로 반환
     */
    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        if (headerNames == null) {
            return headers; // 헤더가 없으면 빈 Map 반환
        }

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = "Authorization".equalsIgnoreCase(headerName)
                    ? "REDACTED"
                    : request.getHeader(headerName);
            headers.put(headerName, headerValue);
        }
        return headers;
    }
}
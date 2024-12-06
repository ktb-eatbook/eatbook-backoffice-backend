package com.eatbook.backoffice.security.auth;

import org.springframework.security.core.Authentication;

import java.util.Map;

public interface AuthTokenProvider <T>{
    T createAuthToken(String id, String role, Map<String, String> claims);
    T convertAuthToken(String token);
    Authentication getAuthentication(T authToken);
}

package com.eatbook.backoffice.security.error.exception;

import com.eatbook.backoffice.global.response.StatusCode;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class JwtTokenException extends AuthenticationException {
    private final StatusCode errorCode;

    public JwtTokenException(StatusCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

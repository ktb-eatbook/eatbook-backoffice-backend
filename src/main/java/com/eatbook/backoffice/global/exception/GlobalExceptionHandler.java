package com.eatbook.backoffice.global.exception;

import com.eatbook.backoffice.global.exception.exceptions.BusinessException;
import com.eatbook.backoffice.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.naming.AuthenticationException;

import static com.eatbook.backoffice.global.response.GlobalErrorCode.*;

/**
 * 백오피스 백엔드 애플리케이션 전역 예외 처리 클래스입니다.
 * 이 클래스는 다양한 예외를 처리하고 클라이언트에 적절한 응답을 제공합니다.
 *
 * @author lavin
 * @since 1.0.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * HTTP 메소드가 지원되지 않는 예외를 처리합니다.
     *
     * @param e HttpRequestMethodNotSupportedException이 발생한 경우.
     * @return HttpRequestMethodNotSupportedException이 발생한 경우, METHOD_NOT_ALLOWED 오류 코드와 설명이 포함된 ApiResponse를 포함하는 ResponseEntity.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ApiResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {

        log.error("[Method Not Allowed] HTTP Method '{}'은(는) 이 요청에 대해 지원되지 않습니다. 지원되는 메소드: {}",
                e.getMethod(), e.getSupportedHttpMethods());

        ApiResponse response = ApiResponse.of(METHOD_NOT_ALLOWED);
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(response);
    }

    /**
     * 인증 예외를 처리합니다.
     *
     * @param e AuthenticationException이 발생한 경우.
     * @return AuthenticationException이 발생한 경우, METHOD_NOT_ALLOWED 오류 코드와 설명이 포함된 ApiResponse를 포함하는 ResponseEntity.
     */
    @ExceptionHandler(AuthenticationException.class)
    protected ResponseEntity<ApiResponse> handleAuthenticationException(final AuthenticationException e) {

        log.error("[Failed to authenticate] 인증 실패: {}", e.getMessage());

        ApiResponse response = ApiResponse.of(METHOD_NOT_ALLOWED, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    /**
     * 처리할 수 없는 API 요청 예외를 처리합니다.
     *
     * @param e NoHandlerFoundException이 발생한 경우.
     * @return NoHandlerFoundException이 발생한 경우, API_NOT_FOUND 오류 코드를 포함하는 ApiResponse를 포함하는 ResponseEntity.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    protected ResponseEntity<ApiResponse> handleNoHandlerFoundException(NoHandlerFoundException e) {

        log.error("[API Not Found] 요청한 API를 찾을 수 없습니다: {}", e.getRequestURL());

        ApiResponse response = ApiResponse.of(API_NOT_FOUND);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    /**
     * 비즈니스 예외를 처리합니다.
     *
     * @param e BusinessException이 발생한 경우.
     * @return BusinessException이 발생한 경우, BAD_REQUEST 상태 코드와 오류 코드, 설명이 포함된 ApiResponse를 포함하는 ResponseEntity.
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ApiResponse> handleBusinessException(final BusinessException e) {

        log.error("[Business Exception] {}", e.getErrorCode(), e.getMessage());

        ApiResponse response = ApiResponse.of(e.getErrorCode());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * {@link HandlerMethodValidationException} 예외를 처리하는 메서드입니다.
     * 해당 예외는 메서드 인자에 대한 유효성 검증 오류가 발생했을 때 던져집니다.
     *
     * @param e {@link HandlerMethodValidationException} 예외 객체
     * @return {@link ResponseEntity} 400 BAD_REQUEST 상태 코드와 함께 유효성 검사 오류에 대한 응답을 반환
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    protected ResponseEntity<ApiResponse> handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        log.error("[Validation Error] {}", e.getMessage(), e);

        ApiResponse response = ApiResponse.of(VALIDATION_ERROR);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * 그 외 처리할 수 없는 예외를 처리합니다.
     *
     * @param e 처리할 수 없는 예외가 발생한 경우.
     * @return 처리할 수 없는 예외가 발생한 경우, INTERNAL_SERVER_ERROR 상태 코드와 오류 코드, 설명이 포함된 ApiResponse를 포함하는 ResponseEntity.
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse> handleException(Exception e) {

        log.error("[Unhandled Exception] 처리할 수 없는 예외가 발생했습니다: {}", e.getMessage(), e);

        ApiResponse response = ApiResponse.of(UNHANDLED_EXCEPTION, e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
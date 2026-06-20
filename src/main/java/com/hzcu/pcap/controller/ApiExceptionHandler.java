package com.hzcu.pcap.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 将控制器抛出的异常统一转换为 JSON 错误响应。
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * 处理带有明确 HTTP 状态码的业务异常。
     *
     * @param exception Spring Web 抛出的状态异常
     * @param request 当前 HTTP 请求
     * @return 标准错误响应
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException exception,
                                                                            HttpServletRequest request) {
        HttpStatusCode statusCode = exception.getStatusCode();
        String message = exception.getReason() == null ? exception.getMessage() : exception.getReason();
        return buildResponse(statusCode, message, request);
    }

    /**
     * 处理服务层抛出的非法状态异常。
     *
     * @param exception 非法状态异常
     * @param request 当前 HTTP 请求
     * @return 标准错误响应
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(IllegalStateException exception,
                                                                          HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), request);
    }

    /**
     * 处理未被更具体处理器捕获的异常。
     *
     * @param exception 原始异常
     * @param request 当前 HTTP 请求
     * @return 标准错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), request);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatusCode statusCode,
                                                             String message,
                                                             HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", statusCode.value());
        body.put("error", reasonPhrase(statusCode));
        body.put("message", message == null ? "" : message);
        body.put("path", request.getRequestURI());
        body.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(statusCode).body(body);
    }

    private String reasonPhrase(HttpStatusCode statusCode) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        return status == null ? "Error" : status.getReasonPhrase();
    }
}

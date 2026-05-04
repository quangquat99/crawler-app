package com.quangph.crawlerapp.exception;

import com.quangph.crawlerapp.dto.response.CrawlResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

/**
 * Handler tập trung cho các exception trả về từ REST API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý lỗi validation request và trả về message ngắn gọn.
     *
     * @param exception exception validation
     * @return response 400 với message lỗi
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CrawlResponse> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Request khong hop le");

        return ResponseEntity.badRequest().body(buildErrorResponse(message));
    }

    /**
     * Xử lý các lỗi chưa được bắt cụ thể trong hệ thống.
     *
     * @param exception exception tổng quát
     * @return response 500 với message lỗi
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<CrawlResponse> handleIllegalState(IllegalStateException exception) {
        return ResponseEntity.badRequest().body(buildErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CrawlResponse> handleGeneric(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(exception.getMessage()));
    }

    private CrawlResponse buildErrorResponse(String message) {
        return new CrawlResponse(
                false,
                null,
                null,
                message == null || message.isBlank() ? "Unexpected server error" : message,
                1,
                10,
                0,
                0,
                Instant.now(),
                List.of()
        );
    }
}

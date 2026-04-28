package com.quangph.crawlerapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

/**
 * Handler tap trung cho cac exception tra ve tu REST API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xu ly loi validation request va tra ve message ngan gon.
     *
     * @param exception exception validation
     * @return response 400 voi message loi
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Request khong hop le");

        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", Instant.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "message", message
        ));
    }

    /**
     * Xu ly cac loi chua duoc bat cu the trong he thong.
     *
     * @param exception exception tong quat
     * @return response 500 voi message loi
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "timestamp", Instant.now(),
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "message", exception.getMessage()
        ));
    }
}

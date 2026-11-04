package com.velas.transitAds.exception;

import com.mongodb.DuplicateKeyException;
import com.velas.transitAds.dto.response.ApiError;
import com.velas.transitAds.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleException(ApiException exception) {
        ApiError error = new ApiError(
                exception.getCode(),
                exception.getFieldErrors()
        );

        return ResponseEntity.status(exception.getStatus()).body(
                ApiResponse.failure(exception.getMessage(), error)
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> fieldErrors =
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .collect(Collectors.toMap(
                                FieldError::getField,
                                error -> error.getDefaultMessage() == null
                                        ? "Invalid value."
                                        : error.getDefaultMessage(),
                                (first, second) -> first
                        ));

        return ResponseEntity.badRequest().body(
                ApiResponse.failure(
                        "Please correct the invalid fields.",
                        new ApiError("VALIDATION_FAILED", fieldErrors)
                )
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableBody(
            HttpMessageNotReadableException ignored
    ) {
        return ResponseEntity.badRequest().body(
                ApiResponse.failure(
                        "The request body is invalid or contains an unsupported value.",
                        new ApiError("INVALID_REQUEST_BODY", Map.of())
                )
        );
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateKey(
            DuplicateKeyException ignored
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiResponse.failure(
                        "A record with one of these unique values already exists.",
                        new ApiError("DUPLICATE_RESOURCE", Map.of())
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(
            Exception ex
    ) {
        log.error("Unexpected error while processing request", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.failure(
                        "An unexpected error occurred. Please try again later.",
                        new ApiError("INTERNAL_SERVER_ERROR", Map.of())
                )
        );
    }
}

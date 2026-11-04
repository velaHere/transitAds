package com.velas.transitAds.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final Map<String, String> fieldErrors;

    public ApiException(HttpStatus status, String code, String message, Map<String, String> fieldErrors) {
        super(message);
        this.status = status;
        this.code = code;
        this.fieldErrors = fieldErrors == null
                ? Map.of()
                : Map.copyOf(fieldErrors);
    }

}

package com.velas.transitAds.dto.response;

import java.time.Instant;
import java.util.function.Function;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        ApiError error,
        Instant timestamp
) {
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(
                true, message, data, null, Instant.now()
        );
    }

    public static <T> ApiResponse<T> failure(
            String message,
            ApiError error
    ) {
        return new ApiResponse<>(
                false, message, null, error, Instant.now()
        );
    }

    public <U> ApiResponse<U> map(Function<T, U> mapper) {
        U mappedData = success ? mapper.apply(data) : null;
        return new ApiResponse<>(
                success,
                message,
                mappedData,
                error,
                timestamp
        );
    }
}

package com.velas.transitAds.dto.response;

import java.util.Map;

public record ApiError(
        String code,
        Map<String, String> fieldErrors
) {
}

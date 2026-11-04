package com.velas.transitAds.controller;

import com.velas.transitAds.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/driver/ping")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<String>> pingDriver() {
        return ResponseEntity.ok(ApiResponse.success("Driver authorized", "Pong Driver!"));
    }

    @GetMapping("/company/ping")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<ApiResponse<String>> pingCompany() {
        return ResponseEntity.ok(ApiResponse.success("Company authorized", "Pong Company!"));
    }
}

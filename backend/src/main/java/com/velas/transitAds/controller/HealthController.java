package com.velas.transitAds.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class HealthController {
    @GetMapping("/health-check")
    public ResponseEntity<?> check(){
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}

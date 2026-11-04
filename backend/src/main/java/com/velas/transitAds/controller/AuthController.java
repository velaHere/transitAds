package com.velas.transitAds.controller;

import com.velas.transitAds.core.domain.AccountType;
import com.velas.transitAds.dto.request.CompanyRegisterRequest;
import com.velas.transitAds.dto.request.DriverRegisterRequest;
import com.velas.transitAds.dto.request.LoginRequest;
import com.velas.transitAds.dto.response.*;
import com.velas.transitAds.entity.AuthenticatedUser;
import com.velas.transitAds.service.AuthService;
import com.velas.transitAds.service.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService){
        this.authService=authService;
    }

    @PostMapping("/register/driver")
    public ResponseEntity<ApiResponse<DriverAuthResponse>> registerDriver(
            @Valid @RequestBody DriverRegisterRequest request
    ){
        AuthResult result = authService.registerDriver(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(
                        HttpHeaders.SET_COOKIE,
                        result.cookie().toString()
                )
                .body(
                        ApiResponse.success(
                                "Driver registered successfully.",
                                result.getDriverAuthResponse()
                        )
                );
    }

    @PostMapping("/register/company")
    public ResponseEntity<ApiResponse<CompanyAuthResponse>> registerCompany(
            @Valid @RequestBody CompanyRegisterRequest request
    ) {
        AuthResult result = authService.registerCompany(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, result.cookie().toString())
                .body(
                        ApiResponse.success(
                                "Company registered successfully.",
                                result.getCompanyAuthResponse()
                        )
                );
    }

    @PostMapping("/login/driver")
    public ResponseEntity<ApiResponse<AuthResponse>> loginDriver(@Valid @RequestBody LoginRequest request) {
        return login(request, AccountType.DRIVER);
    }

    @PostMapping("/login/company")
    public ResponseEntity<ApiResponse<AuthResponse>> loginCompany(@Valid @RequestBody LoginRequest request) {
        return login(request, AccountType.COMPANY);
    }

    private ResponseEntity<ApiResponse<AuthResponse>> login(LoginRequest request, AccountType type) {

        AuthResult authResult = authService.login(request, type);

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, authResult.cookie().toString())
                .body(
                        ApiResponse.success(
                                 "Logged in successfully.",
                                authResult.response()
                        )
                );
    }

    @PostMapping("/verify/{code}")
    public ResponseEntity<ApiResponse<OtpVerificationResponse>> verify(
            @PathVariable String code,
            @AuthenticationPrincipal AuthenticatedUser user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure(
                            "Authenticated user not found",
                            new ApiError("UNAUTHORIZED_USER", Map.of())
                    ));
        }

        OtpVerificationResponse result = authService.verify(user, code);

        return ResponseEntity.ok(
                ApiResponse.success("Account Verified", result)
        );
    }

    @PostMapping("/resend")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure(
                            "Authentication required",
                            new ApiError("UNAUTHORIZED_USER", Map.of())
                    ));
        }
        authService.resendOtp(user, extractClientIp(request));
        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully", null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@CookieValue(name = CookieService.COOKIE_NAME, required = false) String cookie) {
        AuthResult result = authService.refresh(cookie);
        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, result.cookie().toString())
                .body(ApiResponse.success("Token refreshed Successfully", result.response()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal AuthenticatedUser user) {
        if(user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure(
                            "Authentication required",
                            new ApiError("UNAUTHORIZED_USER", Map.of())
                    ));

        ResponseCookie cookie = authService.logout(user);
        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(
                ApiResponse.success("Logged out successfully", null)
                );
    }

    @GetMapping("/check")
    public ResponseEntity<?> check(){
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isBlank()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}

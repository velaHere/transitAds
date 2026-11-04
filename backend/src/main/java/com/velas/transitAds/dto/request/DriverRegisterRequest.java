package com.velas.transitAds.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DriverRegisterRequest(
        @NotBlank @Size(min = 3, max = 30) String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 30) String password,
        @NotBlank @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone number must be a valid 10-digit Indian mobile number") String phone,
        @NotBlank String vehicle,
        @NotBlank String vehicleNo,
        @NotBlank String drivingLicenseUrl,
        @NotBlank String rcUrl
) {
}
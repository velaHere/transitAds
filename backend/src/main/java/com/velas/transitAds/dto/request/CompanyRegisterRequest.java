package com.velas.transitAds.dto.request;

import com.velas.transitAds.core.domain.BusinessType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompanyRegisterRequest(
        @NotBlank @Size(min = 2, max = 100) String companyName,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 64) String password,
        @NotBlank @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone number must be a valid 10-digit Indian mobile number") String phone,
        @NotNull BusinessType businessType,
        String businessRegistrationNumber,
        String gstin
) {
}
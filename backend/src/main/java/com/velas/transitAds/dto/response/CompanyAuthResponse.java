package com.velas.transitAds.dto.response;

import com.velas.transitAds.core.domain.BusinessType;
import com.velas.transitAds.entity.Company;
import com.velas.transitAds.entity.User;

public record CompanyAuthResponse(
        String companyName,
        String email,
        String phone,
        BusinessType businessType,
        String businessRegistrationNumber,
        String gstin,
        long balance,
        boolean verified,
        String accessToken
) implements AuthResponse {
    public static CompanyAuthResponse generate(User user, Company company, String accessToken) {
        return new CompanyAuthResponse(
                company.getCompanyName(),
                user.getEmail(),
                company.getPhone(),
                company.getBusinessType(),
                company.getBusinessRegistrationNumber(),
                company.getGstin(),
                company.getBalance(),
                user.isVerified(),
                accessToken
        );
    }
}

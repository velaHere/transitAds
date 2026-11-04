package com.velas.transitAds.dto.response;

import org.springframework.http.ResponseCookie;

public record AuthResult(
        AuthResponse response,
        ResponseCookie cookie
) {
    public DriverAuthResponse getDriverAuthResponse() {
        if(!(response instanceof DriverAuthResponse driverAuthResponse))
            throw new IllegalStateException("Unable to cast the AuthResponse to DriverAuthResponse");
        return driverAuthResponse;
    }

    public CompanyAuthResponse getCompanyAuthResponse() {
        if(!(response instanceof CompanyAuthResponse companyAuthResponse))
            throw new IllegalStateException("Unable to cast the AuthResponse to CompanyAuthResponse");
        return companyAuthResponse;
    }
}

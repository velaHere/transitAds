package com.velas.transitAds.dto.response;

import com.velas.transitAds.entity.Driver;
import com.velas.transitAds.entity.User;

public record DriverAuthResponse(
        String name,
        String email,
        String phone,
        long balance,
        String vehicle,
        String vehicleNo,
        String drivingLicense,
        String rc,
        boolean verified,
        String accessToken
) implements AuthResponse {
    public static DriverAuthResponse generate(User user, Driver driver, String accessToken) {
        return new DriverAuthResponse(
                driver.getName(),
                user.getEmail(),
                driver.getPhone(),
                driver.getBalance(),
                driver.getVehicle().toString(),
                driver.getVehicleNo(),
                driver.getDrivingLicenseUrl(),
                driver.getRcUrl(),
                user.isVerified(),
                accessToken
        );
    }
}

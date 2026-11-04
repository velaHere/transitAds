package com.velas.transitAds.service;

import com.velas.transitAds.core.domain.AccountType;
import com.velas.transitAds.core.domain.Vehicle;
import com.velas.transitAds.dto.request.CompanyRegisterRequest;
import com.velas.transitAds.dto.request.DriverRegisterRequest;
import com.velas.transitAds.dto.request.LoginRequest;
import com.velas.transitAds.dto.response.*;
import com.velas.transitAds.entity.AuthenticatedUser;
import com.velas.transitAds.entity.Company;
import com.velas.transitAds.entity.Driver;
import com.velas.transitAds.entity.User;
import com.velas.transitAds.exception.ApiException;
import com.velas.transitAds.repository.CompanyRepository;
import com.velas.transitAds.repository.DriverRepository;
import com.velas.transitAds.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AuthService {

    private final EmailService emailService;
    private final OtpService otpService;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final DriverRepository driverRepository;
    private final CookieService cookieService;
    private final AccessTokenService accessTokenService;
    private final BCryptPasswordEncoder encoder;
    private final String dummyPasswordHash;


    @Autowired
    public AuthService(
            EmailService emailService,
            OtpService otpService,
            UserRepository userRepository,
            CompanyRepository companyRepository,
            DriverRepository driverRepository,
            CookieService cookieService,
            BCryptPasswordEncoder encoder,
            AccessTokenService accessTokenService
    ){
        this.emailService = emailService;
        this.otpService = otpService;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.driverRepository=driverRepository;
        this.cookieService = cookieService;
        this.accessTokenService = accessTokenService;
        this.encoder = encoder;
        this.dummyPasswordHash = encoder.encode(UUID.randomUUID().toString());
    }

    public AuthResult registerDriver(@NotNull DriverRegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        String normalizedPhone = request.phone().trim();
        String normalizedVehicleNo = request.vehicleNo().trim().toUpperCase(Locale.ROOT);

        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "EMAIL_ALREADY_REGISTERED",
                    "An account with this email already exists.",
                    Map.of("email", "This email is already registered.")
            );
        }

        if (driverRepository.existsByPhone(normalizedPhone)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "PHONE_ALREADY_REGISTERED",
                    "An account with this phone number already exists.",
                    Map.of("phone", "This phone is already registered.")
            );
        }

        if (driverRepository.existsByVehicleNo(normalizedVehicleNo)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "VEHICLE_NO_ALREADY_REGISTERED",
                    "A vehicle with this registration number already exists.",
                    Map.of("vehicleNo", "This vehicle is already registered.")
            );
        }

        User user = createBaseUser(normalizedEmail, request.password(), AccountType.DRIVER);

        Driver driver = Driver.builder()
                .id(user.getId())
                .name(request.name().trim())
                .phone(normalizedPhone)
                .balance(0L)
                .vehicle(Vehicle.valueOf(request.vehicle().trim().toUpperCase(Locale.ROOT)))
                .vehicleNo(normalizedVehicleNo)
                .drivingLicenseUrl(request.drivingLicenseUrl().trim())
                .rcUrl(request.rcUrl().trim())
                .build();

        userRepository.insert(user);
        try {
            driverRepository.insert(driver);
        } catch (Exception ex) {
            userRepository.deleteById(user.getId());
            throw ex;
        }

        String rawOTP = otpService.generateOTP(user.getId().toString());
        emailService.sendVerificationEmail(user.getEmail(), rawOTP);

        String accessToken = accessTokenService.generateAccessToken(user.getEmail());
        ResponseCookie cookie = cookieService.generateCookie(driver.getId().toString());

        return new AuthResult(
                DriverAuthResponse.generate(user, driver, accessToken),
                cookie
        );
    }

    public AuthResult registerCompany(@NotNull CompanyRegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        String normalizedPhone = request.phone().trim();
        String normalizedGstin = request.gstin() != null && !request.gstin().isBlank()
                ? request.gstin().trim().toUpperCase(Locale.ROOT)
                : null;
        String normalizedRegNo = request.businessRegistrationNumber() != null && !request.businessRegistrationNumber().isBlank()
                ? request.businessRegistrationNumber().trim().toUpperCase(Locale.ROOT)
                : null;

        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "EMAIL_ALREADY_REGISTERED",
                    "An account with this email already exists.",
                    Map.of("email", "This email is already registered.")
            );
        }

        if (companyRepository.existsByPhone(normalizedPhone)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "PHONE_ALREADY_REGISTERED",
                    "An account with this phone number already exists.",
                    Map.of("phone", "This phone is already registered.")
            );
        }

        if (normalizedRegNo != null && companyRepository.existsByBusinessRegistrationNumber(normalizedRegNo)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "REGISTRATION_NUMBER_ALREADY_REGISTERED",
                    "A business with this registration number already exists.",
                    Map.of("businessRegistrationNumber", "This registration number is already registered.")
            );
        }

        if (normalizedGstin != null && companyRepository.existsByGstin(normalizedGstin)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "GSTIN_ALREADY_REGISTERED",
                    "A company with this GSTIN already exists.",
                    Map.of("gstin", "This GSTIN is already registered.")
            );
        }

        User user = createBaseUser(normalizedEmail, request.password(), AccountType.COMPANY);

        Company company = Company.builder()
                .id(user.getId())
                .companyName(request.companyName().trim())
                .phone(normalizedPhone)
                .businessType(request.businessType())
                .businessRegistrationNumber(normalizedRegNo)
                .gstin(normalizedGstin)
                .balance(0L)
                .build();

        userRepository.insert(user);
        try {
            companyRepository.insert(company);
        } catch (Exception ex) {
            userRepository.deleteById(user.getId());
            throw ex;
        }

        String rawOTP = otpService.generateOTP(user.getId().toString());
        emailService.sendVerificationEmail(user.getEmail(), rawOTP);

        String accessToken = accessTokenService.generateAccessToken(user.getEmail());
        ResponseCookie cookie = cookieService.generateCookie(company.getId().toString());

        return new AuthResult(
                CompanyAuthResponse.generate(user, company, accessToken),
                cookie
        );
    }

    private User createBaseUser(String normalizedEmail, String rawPassword, AccountType accountType) {
        return User.builder()
                .id(new ObjectId())
                .email(normalizedEmail)
                .password(encoder.encode(rawPassword))
                .accountType(accountType)
                .roles(List.of(accountType.name()))
                .verified(false)
                .build();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public AuthResult login(@NotNull LoginRequest request, @NotNull AccountType accountType) {

        Optional<User> optionalUser = userRepository.findByEmail(request.email().toLowerCase().trim());

        String passwordHash = optionalUser
                .map(User::getPassword)
                .orElse(dummyPasswordHash);

        boolean passwordMatches = encoder.matches(
                request.password(),
                passwordHash
        );

        if (optionalUser.isEmpty() || !passwordMatches) {
            throw invalidCredentials();
        }

        User user = optionalUser.get();

        if(!user.getAccountType().equals(accountType)) {
            throw invalidCredentials();
        }

        return switch (user.getAccountType()) {

            case DRIVER -> {
                Driver driver = driverRepository.findById(user.getId())
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "DRIVER_PROFILE_NOT_FOUND",
                                "The driver account profile could not be found",
                                Map.of("userId", user.getId().toString())
                        ));

                String accessToken =
                        accessTokenService.generateAccessToken(user.getEmail());

                yield new AuthResult(
                        DriverAuthResponse.generate(user, driver, accessToken),
                        cookieService.generateCookie(driver.getId().toString())
                );
            }

            case COMPANY -> {

                Company company = companyRepository.findById(user.getId()).orElseThrow(
                        () -> new ApiException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "COMPANY_PROFILE_NOT_FOUND",
                                "The company account profile could not be found",
                                Map.of("userId", user.getId().toString())
                        )
                );

                String accessToken =
                        accessTokenService.generateAccessToken(user.getEmail());

                yield new AuthResult(
                        CompanyAuthResponse.generate(user, company, accessToken),
                        cookieService.generateCookie(company.getId().toString())
                );
            }
        };
    }

    public AuthResult refresh(String cookie) {
        if(cookie == null || cookie.isBlank())
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "MISSING_SESSION_COOKIE",
                    "Refresh cookie is required",
                    Map.of()
            );

        String userId = cookieService.verifyAndGetUserId(cookie);
        if (userId == null || !ObjectId.isValid(userId)) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "INVALID_SESSION",
                    "Session is invalid or expired. Please log in again.",
                    Map.of()
            );
        }

        User user = userRepository.findById(new ObjectId(userId)).orElseThrow(() -> new ApiException(
                HttpStatus.UNAUTHORIZED,
                "USER_NOT_FOUND",
                "User account no longer exists",
                Map.of()
        ));

        return switch (user.getAccountType()) {
            case DRIVER -> {
                Driver driver = driverRepository.findById(user.getId())
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "DRIVER_PROFILE_NOT_FOUND",
                                "Driver profile not found",
                                Map.of()
                        ));

                String accessToken = accessTokenService.generateAccessToken(user.getEmail());

                yield new AuthResult(DriverAuthResponse.generate(user, driver, accessToken), cookieService.generateCookie(userId));
            }
            case COMPANY -> {

                Company company = companyRepository.findById(user.getId())
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "COMPANY_PROFILE_NOT_FOUND",
                                "Company profile not found",
                                Map.of()
                        ));

                String accessToken = accessTokenService.generateAccessToken(user.getEmail());

                yield new AuthResult(
                        CompanyAuthResponse.generate(user, company, accessToken),
                        cookieService.generateCookie(userId)
                );
            }
        };
    }

    @NotNull
    public ResponseCookie logout(@NotNull AuthenticatedUser user) {
        cookieService.invalidate(user.id());
        accessTokenService.deleteJWT(user.email());
        return cookieService.getCleanCookie();
    }

    private ApiException invalidCredentials() {
        return new ApiException(
                HttpStatus.UNAUTHORIZED,
                "INVALID_CREDENTIALS",
                "Either email or password is incorrect",
                Map.of()
        );
    }

    public OtpVerificationResponse verify(AuthenticatedUser user, String otp) {
        if (user.verified())
            return new OtpVerificationResponse(true);

        long check = otpService.reserveVerificationAttempt(user.id());

        if(check == -1)
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "OTP_EXPIRED",
                    "Otp expired",
                    Map.of()
            );
        else if(check == 0)
            throw new ApiException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "TOO_MANY_REQUESTS",
                    "Too many otp verification requests",
                    Map.of()
            );

        boolean matches = otpService.verifyOtp(user.id(), otp);

        if(matches) {
            userRepository.update(new ObjectId(user.id()), "verified", true);
            otpService.deleteOTP(user.id());
            return new OtpVerificationResponse(true);
        }

        throw new ApiException(
                HttpStatus.BAD_REQUEST,
                "OTP_VERIFICATION_FAILED",
                "Otp verification failed",
                Map.of()
        );
    }

    public void resendOtp(AuthenticatedUser user, String clientIp) {

        if(user.verified())
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "ALREADY_VERIFIED",
                    "Your account is already verified.",
                    Map.of()
            );

        long limitStatus = otpService.reserveResend(user.id(), clientIp);

        if (limitStatus == 1) {
            throw new ApiException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "COOLDOWN_ACTIVE",
                    "Please wait before requesting another code.",
                    Map.of()
            );
        } else if (limitStatus == 2) {
            throw new ApiException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "USER_RATE_LIMIT_EXCEEDED",
                    "You have exceeded the maximum hourly OTP attempts.",
                    Map.of()
            );
        } else if (limitStatus == 3) {
            throw new ApiException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "IP_RATE_LIMIT_EXCEEDED",
                    "Too many OTP requests from your network. Please try again later.",
                    Map.of()
            );
        }

        String rawOtp = otpService.generateOTP(user.id());
        emailService.sendVerificationEmail(user.email(), rawOtp);
    }
}

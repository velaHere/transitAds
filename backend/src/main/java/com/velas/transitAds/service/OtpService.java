package com.velas.transitAds.service;

import com.velas.transitAds.config.property.AppProperties;
import com.velas.transitAds.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class OtpService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final OtpRepository otpRepository;
    private final SecretKeySpec secretKeySpec;
    private final SecureRandom random;

    @Autowired
    public OtpService(OtpRepository otpRepository, AppProperties properties) {
        this.otpRepository = otpRepository;
        this.random = new SecureRandom();

        // Using existing secret from application properties
        byte[] secretBytes = properties.security().stimulus().secret().getBytes(StandardCharsets.UTF_8);
        this.secretKeySpec = new SecretKeySpec(secretBytes, HMAC_ALGORITHM);
    }

    public String generateOTP(String userID) {
        String otp = String.format("%06d", random.nextInt(1_000_000));
        String hashedOTP = hashOtp(userID, otp);
        otpRepository.saveHashedOTP(userID, hashedOTP);
        return otp;
    }

    public boolean verifyOtp(String userID, String rawOtp) {
        String storedHash = otpRepository.getHashedOTP(userID);
        if (storedHash == null) {
            return false;
        }

        String candidateHash = hashOtp(userID, rawOtp);

        return MessageDigest.isEqual(
                candidateHash.getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8)
        );
    }

    public long reserveVerificationAttempt(String userID) {
        return this.otpRepository.reserveAttempt(userID);
    }

    public long reserveResend(String userID, String clientIp) {
        return this.otpRepository.reserveResend(userID, clientIp);
    }

    public void deleteOTP(String userID) {
        otpRepository.deleteHashedOTP(userID);
    }

    private String hashOtp(String userId, String rawOtp) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] bytes = mac.doFinal((rawOtp + ":" + userId).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to calculate OTP HMAC", e);
        }
    }
}

package com.velas.transitAds.service;

import com.velas.transitAds.config.property.AppProperties;
import com.velas.transitAds.repository.CookieRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@Service
public class CookieService {

    private static final SecureRandom sRandom=new SecureRandom();
    public static final String COOKIE_NAME = "stimulus";

    private final String ALGORITHM;
    private final String APPENDER;
    private final SecretKeySpec secretKeySpec;
    private final CookieRepository cookieRepository;

    @Autowired
    public CookieService(
            AppProperties props,
            CookieRepository cookieRepository
    ){
        this.ALGORITHM=props.security().stimulus().algorithm();
        this.APPENDER=props.security().stimulus().appender();
        this.cookieRepository = cookieRepository;
        this.secretKeySpec=new SecretKeySpec(
                props.security().stimulus().secret().getBytes(StandardCharsets.UTF_8),
                ALGORITHM
        );
    }

    public void invalidate(String userID) {
        cookieRepository.deleteSignature(userID);
    }

    public ResponseCookie generateCookie(String userId) {
        long signature = sRandom.nextLong();
        String payload = userId + APPENDER + signature;

        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);

        String encodedPayload = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payloadBytes);

        String encodedMac = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(calculateMac(payloadBytes));

        String cookieValue = encodedPayload + "." + encodedMac;

        cookieRepository.saveSignature(userId, signature);

        return ResponseCookie.from(COOKIE_NAME, cookieValue)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/auth/refresh")
                .maxAge(Duration.ofDays(14))
                .build();
    }

    public boolean verifyCookie(String cookie) {
        return verifyAndGetUserId(cookie) != null;
    }

    @Nullable
    public String verifyAndGetUserId(String cookie) {
        if (cookie == null || cookie.isBlank()) {
            return null;
        }

        try {
            int separator = cookie.indexOf('.');

            // Require exactly one separator.
            if (separator <= 0
                    || separator != cookie.lastIndexOf('.')
                    || separator == cookie.length() - 1) {
                return null;
            }

            String encodedPayload = cookie.substring(0, separator);
            String encodedMac = cookie.substring(separator + 1);

            byte[] payloadBytes =
                    Base64.getUrlDecoder().decode(encodedPayload);

            byte[] suppliedMac =
                    Base64.getUrlDecoder().decode(encodedMac);

            // Verify the HMAC before trusting the payload.
            byte[] expectedMac = calculateMac(payloadBytes);

            if (!MessageDigest.isEqual(expectedMac, suppliedMac)) {
                return null;
            }

            String payload = new String(
                    payloadBytes,
                    StandardCharsets.UTF_8
            );

            int delimiter = payload.indexOf(APPENDER);

            if (delimiter <= 0
                    || delimiter != payload.lastIndexOf(APPENDER)
                    || delimiter + APPENDER.length() >= payload.length()) {
                return null;
            }

            String userId = payload.substring(0, delimiter);
            String suppliedSignature =
                    payload.substring(delimiter + APPENDER.length());

            Optional<Long> storedSignature =
                    cookieRepository.getSignature(userId);

            if (storedSignature.isEmpty()) {
                return null;
            }

            String expectedSignature =
                    Long.toString(storedSignature.get());

            if (!MessageDigest.isEqual(
                    suppliedSignature.getBytes(StandardCharsets.UTF_8),
                    expectedSignature.getBytes(StandardCharsets.UTF_8))) {
                return null;
            }

            return userId;

        } catch (IllegalArgumentException e) {
            // Invalid Base64URL or malformed input.
            return null;
        }
    }

    @NotNull
    public ResponseCookie getCleanCookie() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/auth/refresh")
                .maxAge(0)
                .build();
    }

    private byte[] calculateMac(byte[] payload) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(secretKeySpec);
            return mac.doFinal(payload);
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException("Failed to calculate cookie HMAC", e);
        }
    }
}
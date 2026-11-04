package com.velas.transitAds.repository;

import com.velas.transitAds.config.property.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OtpRepository {

    private final String keyPrefix;
    private final String hashedOtpField;
    private final String attemptsField;

    private static final RedisScript<Long> RESERVE_ATTEMPT_SCRIPT =
            RedisScript.of(
                    new ClassPathResource(
                            "otp/reserve_attempt.lua"
                    ),
                    Long.class
            );

    private static final RedisScript<Long> RESERVE_RESEND_RATE_LIMIT =
            RedisScript.of(
                    new ClassPathResource(
                            "otp/resend_rate_limit.lua"
                    ),
                    Long.class
            );

    private static final RedisScript<Long> SAVE_OTP_SCRIPT =
            RedisScript.of(
                    new ClassPathResource("otp/save_otp.lua"),
                    Long.class
            );

    private final RedisTemplate<String, Object> template;

    @Autowired
    public OtpRepository(RedisTemplate<String, Object> template, AppProperties properties) {
        this.template = template;
        this.keyPrefix = properties.security().otp().cacheKeyPrefix();
        this.hashedOtpField = properties.security().otp().hashedOtpField();
        this.attemptsField = properties.security().otp().attemptsField();
    }

    public void saveHashedOTP(String userID, String hashedOTP) {
        Long result = template.execute(
                SAVE_OTP_SCRIPT,
                List.of(keyPrefix + userID),
                hashedOTP,
                300,
                hashedOtpField,
                attemptsField
        );

        if (!Long.valueOf(1L).equals(result)) {
            throw new IllegalStateException("Failed to save OTP");
        }
    }

    public void deleteHashedOTP(String userId) {
        String key = keyPrefix + userId;
        template.delete(key);
    }

    public String getHashedOTP(String userID) {
        return (String) template.opsForHash().get(keyPrefix + userID, hashedOtpField);
    }

    public long reserveAttempt(String userID) {
        return template.execute(RESERVE_ATTEMPT_SCRIPT, List.of(keyPrefix +userID));
    }

    public long reserveResend(String userID, String ip) {
        return template.execute(
                RESERVE_RESEND_RATE_LIMIT,
                List.of(
                        resendCooldown(userID),
                        resendUserHourly(userID),
                        resendIpHourly(ip)
                ),
                30, 5, 20, 3600
        );
    }

    private static String resendCooldown(String userID) {
        return "otp:resend:user:" + userID + ":cooldown";
    }

    private static String resendUserHourly(String userID) {
        return "otp:resend:user:" + userID;
    }

    private static String resendIpHourly(String ip) {
        return "otp:resend:ip:" + ip.replace(":", "_");
    }
}

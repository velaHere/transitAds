package com.velas.transitAds.service;

import com.velas.transitAds.config.property.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;


@Service
public class AccessTokenService {

    private final SecretKey secretKey;
    private final String issuer;
    private final long accessTokenMinutes;
    private final RedisTemplate<String, Object> redisTemplate;
    private final String ACCESS_TOKEN_PREFIX = "ACCESS_TOKEN:";

    @Autowired
    public AccessTokenService(AppProperties props,
                              RedisTemplate<String, Object> redisTemplate){
        byte[] keyBytes = Base64.getDecoder().decode(props.security().accessToken().secret());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.issuer=props.security().accessToken().issuer();
        this.accessTokenMinutes=props.security().accessToken().ttlMins();
        this.redisTemplate=redisTemplate;
    }

    @Nullable
    public String verifyAndExtractUsername(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        try {
            String subject = extractAllClaims(token).getSubject();
            return subject == null || subject.isBlank() ? null : subject;
        } catch (io.jsonwebtoken.JwtException
                 | IllegalArgumentException ex) {
            return null;
        }
    }

    @Nullable
    public Date extractExpiration(String token){
        try {
            return extractAllClaims(token).getExpiration();
        } catch (Exception e) {
            return null;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenExpiredOrInvalid(@Nullable String token) {
        if (token == null || token.isBlank()) {
            return true;
        }

        try {
            Claims claims = extractAllClaims(token);

            String username = claims.getSubject();
            if (username == null || username.isBlank()) {
                return true;
            }

            Object stored = redisTemplate.opsForValue()
                    .get(ACCESS_TOKEN_PREFIX + username);

            return !token.equals(stored);

        } catch (io.jsonwebtoken.JwtException
                 | IllegalArgumentException ex) {
            return true;
        }
    }

    public void deleteJWT(String username) {
        redisTemplate.delete(ACCESS_TOKEN_PREFIX + username);
    }

    public String generateAccessToken(String username) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(accessTokenMinutes * 60);

        String token = Jwts.builder()
                .issuer(issuer)
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();

        redisTemplate.opsForValue().set(
                ACCESS_TOKEN_PREFIX + username,
                token,
                accessTokenMinutes,
                TimeUnit.MINUTES
        );

        return token;
    }
}

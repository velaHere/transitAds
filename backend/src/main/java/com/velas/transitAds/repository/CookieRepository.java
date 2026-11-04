package com.velas.transitAds.repository;

import com.velas.transitAds.config.property.AppProperties;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
public class CookieRepository {

    public final String COOKIE_SIGNATURE = "COOKIE:SIGNATURE:";
    private final int ttlDays;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public CookieRepository(AppProperties props, RedisTemplate<String, Object> redisTemplate){
        this.redisTemplate=redisTemplate;
        this.ttlDays=props.security().stimulus().ttlDays();
    }

    public void saveSignature(@NotNull String userId, long signature){
        String redisKey = COOKIE_SIGNATURE + userId;
        redisTemplate.opsForValue().set(redisKey, signature, ttlDays, TimeUnit.DAYS);
    }

    public Optional<Long> deleteSignature(String userId) {
        Object signature = redisTemplate.opsForValue().getAndDelete(COOKIE_SIGNATURE + userId);
        if(signature == null) return Optional.empty();
        return Optional.of((long) signature);
    }

    public Optional<Long> getSignature(String userId) {
        Object signature = redisTemplate.opsForValue().get(COOKIE_SIGNATURE + userId);
        if(signature == null) return Optional.empty();
        return Optional.of((long) signature);
    }
}

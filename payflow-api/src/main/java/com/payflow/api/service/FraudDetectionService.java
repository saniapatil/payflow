package com.payflow.api.service;
import com.payflow.api.dto.FraudResult;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
@Service
public class FraudDetectionService {
    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("50000");
    private static final int VELOCITY_LIMIT = 3;
    private final StringRedisTemplate redisTemplate;
    public FraudDetectionService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    public FraudResult analyze(Long senderId, BigDecimal amount) {
        String countKey = "txn:count:" + senderId;
        Long count = redisTemplate.opsForValue().increment(countKey);
        if (count != null && count == 1) {
            redisTemplate.expire(countKey, Duration.ofMinutes(1));
        }
        int score = 0;
        String reason = "Normal transaction";
        if (amount.compareTo(HIGH_AMOUNT_THRESHOLD) > 0) {
            score += 60;
            reason = "Unusually large amount";
        }
        if (count != null && count > VELOCITY_LIMIT) {
            score += 50;
            reason = "Too many transfers in a short window";
        }
        int hour = LocalTime.now().getHour();
        if (hour >= 1 && hour <= 5) {
            score += 20;
            reason = score > 50 ? reason : "Off-hours transaction";
        }
        return new FraudResult(Math.min(score, 100), reason, score >= 85);
    }
}

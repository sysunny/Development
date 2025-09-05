package com.example.payment.gateway;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.example.payment.exception.RateLimitExceededException;
import com.example.payment.model.PaymentRequest;
import com.example.payment.model.Transaction;

@Component
public class PaymentGatewayProxy {

    private final Map<String, SimpleRateLimiter> limiters = new ConcurrentHashMap<>();

    private SimpleRateLimiter resolveLimiter(String key) {
        return limiters.computeIfAbsent(key, k -> new SimpleRateLimiter(10, Duration.ofMinutes(1)));
    }

    public Transaction processPayment(PaymentRequest request, PaymentGateway gateway) {
        // Basic validation beyond bean validation
        if (request == null || !StringUtils.hasText(request.getSender()) || !StringUtils.hasText(request.getReceiver())) {
            throw new IllegalArgumentException("Invalid payment request: sender and receiver are required");
        }

        String key = request.getSender();
        SimpleRateLimiter limiter = resolveLimiter(key);
        boolean allowed = limiter.tryConsume();
        if (!allowed) {
            throw new RateLimitExceededException("Rate limit exceeded for sender: " + key);
        }

        // Delegate to concrete gateway
        return gateway.processPayment(request);
    }

    // Simple leaky-bucket / token-bucket style rate limiter
    private static class SimpleRateLimiter {
        private final int capacity;
        private final Duration refillPeriod;
        private double tokens;
        private Instant lastRefill;

        SimpleRateLimiter(int capacity, Duration refillPeriod) {
            this.capacity = capacity;
            this.refillPeriod = refillPeriod;
            this.tokens = capacity;
            this.lastRefill = Instant.now();
        }

        synchronized boolean tryConsume() {
            refillTokens();
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }

        private void refillTokens() {
            Instant now = Instant.now();
            long millisSince = Duration.between(lastRefill, now).toMillis();
            if (millisSince <= 0) return;
            double refillRatio = (double) millisSince / refillPeriod.toMillis();
            if (refillRatio > 0) {
                tokens = Math.min(capacity, tokens + refillRatio * capacity);
                lastRefill = now;
            }
        }
    }
}

package com.example.payment.service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.payment.factory.GatewayFactory;
import com.example.payment.gateway.PaymentGateway;
import com.example.payment.gateway.PaymentGatewayProxy;
import com.example.payment.model.PaymentRequest;
import com.example.payment.model.Transaction;
import com.example.payment.repository.TransactionRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class PaymentService {

    private final GatewayFactory gatewayFactory;
    private final PaymentGatewayProxy proxy;
    private final TransactionRepository transactionRepository;
    private final TransactionSaver transactionSaver;

    // Per-idempotency-key locks to serialize concurrent saves and avoid DB constraint churn
    private final ConcurrentMap<String, Object> saveLocks = new ConcurrentHashMap<>();

    public PaymentService(GatewayFactory gatewayFactory, PaymentGatewayProxy proxy, TransactionRepository transactionRepository, TransactionSaver transactionSaver) {
        this.gatewayFactory = gatewayFactory;
        this.proxy = proxy;
        this.transactionRepository = transactionRepository;
        this.transactionSaver = transactionSaver;
    }

    @CircuitBreaker(name = "paymentService", fallbackMethod = "fallback")
    public Transaction processPayment(PaymentRequest request, String idempotencyKey) {
        // If idempotency key provided, return existing transaction when present
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        PaymentGateway gateway = gatewayFactory.getGateway(request.getGatewayType());
        Transaction tx = proxy.processPayment(request, gateway);
        tx.setIdempotencyKey(idempotencyKey);

        // If no idempotency key provided, just save normally
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            return persistTransaction(tx);
        }

        // Acquire per-key lock to serialize concurrent inserts for the same idempotency key.
        Object lock = saveLocks.computeIfAbsent(idempotencyKey, k -> new Object());
        synchronized (lock) {
            try {
                // Re-check inside lock to avoid race
                Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
                if (existing.isPresent()) {
                    return existing.get();
                }
                return persistTransaction(tx);
            } finally {
                // Remove the lock to prevent map growth. Only remove if still mapped to this lock instance.
                saveLocks.remove(idempotencyKey, lock);
            }
        }
    }

    // Fallback invoked by Resilience4j when circuit is open or errors occur
    public Transaction fallback(PaymentRequest request, String idempotencyKey, Throwable t) {
        Transaction tx = new Transaction();
        tx.setSender(request != null ? request.getSender() : null);
        tx.setReceiver(request != null ? request.getReceiver() : null);
        tx.setAmount(request != null ? request.getAmount() : null);
        tx.setCurrency(request != null ? request.getCurrency() : null);
        tx.setGatewayType(request != null ? request.getGatewayType() : null);
        tx.setStatus("FAILED_CIRCUIT_BREAKER");
        tx.setIdempotencyKey(idempotencyKey);
        return persistTransaction(tx);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected Transaction findExistingByIdempotencyKey(String idempotencyKey) {
        // Use trim/isEmpty to remain compatible with older Java versions in the CI environment
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) return null;
        return transactionRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
    }

    // Use TransactionSaver if available (REQUIRES_NEW); otherwise fall back to repository.save
    private Transaction persistTransaction(Transaction tx) {
        if (this.transactionSaver != null) {
            return this.transactionSaver.saveNew(tx);
        }
        return this.transactionRepository.save(tx);
    }
}

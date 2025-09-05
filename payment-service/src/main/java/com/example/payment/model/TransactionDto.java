package com.example.payment.model;

import java.math.BigDecimal;
import java.time.Instant;

public class TransactionDto {
    private Long id;
    private String sender;
    private String receiver;
    private BigDecimal amount;
    private String currency;
    private GatewayType gatewayType;
    private String status;
    private Instant createdAt;

    // getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public GatewayType getGatewayType() {
        return gatewayType;
    }

    public void setGatewayType(GatewayType gatewayType) {
        this.gatewayType = gatewayType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public static TransactionDto from(Transaction tx) {
        if (tx == null) return null;
        TransactionDto d = new TransactionDto();
        d.setId(tx.getId());
        d.setSender(tx.getSender());
        d.setReceiver(tx.getReceiver());
        d.setAmount(tx.getAmount());
        d.setCurrency(tx.getCurrency());
        d.setGatewayType(tx.getGatewayType());
        d.setStatus(tx.getStatus());
        d.setCreatedAt(tx.getCreatedAt());
        return d;
    }
}

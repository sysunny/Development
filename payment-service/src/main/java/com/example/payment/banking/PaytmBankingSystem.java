package com.example.payment.banking;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.example.payment.model.PaymentRequest;
import com.example.payment.model.Transaction;

@Component
public class PaytmBankingSystem {

    public Transaction process(PaymentRequest request) {
        Transaction tx = new Transaction();
        tx.setSender(request != null ? request.getSender() : null);
        tx.setReceiver(request != null ? request.getReceiver() : null);
        tx.setAmount(request != null ? request.getAmount() : null);
        tx.setCurrency(request != null ? request.getCurrency() : null);
        tx.setStatus("SUCCESS_PAYTM");
        tx.setCreatedAt(Instant.now());
        return tx;
    }
}

package com.example.payment.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.payment.model.Transaction;
import com.example.payment.repository.TransactionRepository;

@Component
public class TransactionSaver {

    private final TransactionRepository transactionRepository;

    public TransactionSaver(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transaction saveNew(Transaction tx) {
        // Copy into a fresh entity instance to ensure a clean persistence context
        Transaction toSave = new Transaction();
        toSave.setSender(tx.getSender());
        toSave.setReceiver(tx.getReceiver());
        toSave.setAmount(tx.getAmount());
        toSave.setCurrency(tx.getCurrency());
        toSave.setGatewayType(tx.getGatewayType());
        toSave.setStatus(tx.getStatus());
        toSave.setCreatedAt(tx.getCreatedAt());
        toSave.setIdempotencyKey(tx.getIdempotencyKey());
        return transactionRepository.save(toSave);
    }
}

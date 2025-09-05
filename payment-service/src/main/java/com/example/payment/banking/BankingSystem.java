package com.example.payment.banking;

import com.example.payment.model.PaymentRequest;
import com.example.payment.model.Transaction;

public abstract class BankingSystem {
    public abstract Transaction process(PaymentRequest request);
}

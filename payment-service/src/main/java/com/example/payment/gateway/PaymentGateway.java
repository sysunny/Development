package com.example.payment.gateway;

import com.example.payment.model.PaymentRequest;
import com.example.payment.model.Transaction;

public interface PaymentGateway {
    Transaction processPayment(PaymentRequest request);
}

package com.example.payment.gateway;

import org.springframework.stereotype.Component;

import com.example.payment.banking.PaytmBankingSystem;
import com.example.payment.model.PaymentRequest;
import com.example.payment.model.Transaction;

@Component("paytmGateway")
public class PaytmGateway implements PaymentGateway {

    private final PaytmBankingSystem paytmBankingSystem;

    public PaytmGateway(PaytmBankingSystem paytmBankingSystem) {
        this.paytmBankingSystem = paytmBankingSystem;
    }

    @Override
    public Transaction processPayment(PaymentRequest request) {
        // Add any gateway-specific transformation if needed
        return paytmBankingSystem.process(request);
    }
}

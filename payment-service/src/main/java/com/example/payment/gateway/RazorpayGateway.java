package com.example.payment.gateway;

import org.springframework.stereotype.Component;

import com.example.payment.banking.RazorpayBankingSystem;
import com.example.payment.model.PaymentRequest;
import com.example.payment.model.Transaction;

@Component("razorpayGateway")
public class RazorpayGateway implements PaymentGateway {

    private final RazorpayBankingSystem razorpayBankingSystem;

    public RazorpayGateway(RazorpayBankingSystem razorpayBankingSystem) {
        this.razorpayBankingSystem = razorpayBankingSystem;
    }

    @Override
    public Transaction processPayment(PaymentRequest request) {
        // Any Razorpay-specific logic could go here
        return razorpayBankingSystem.process(request);
    }
}

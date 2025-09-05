package com.example.payment.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.payment.factory.GatewayFactory;
import com.example.payment.gateway.PaymentGateway;
import com.example.payment.gateway.PaymentGatewayProxy;
import com.example.payment.model.GatewayType;
import com.example.payment.model.PaymentRequest;
import com.example.payment.model.Transaction;
import com.example.payment.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private GatewayFactory gatewayFactory;

    @Mock
    private PaymentGatewayProxy proxy;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private PaymentService paymentService;

    @Captor
    private ArgumentCaptor<Transaction> txCaptor;

    private PaymentRequest request;

    @BeforeEach
    void setUp() {
        request = new PaymentRequest();
        request.setSender("alice@example.com");
        request.setReceiver("bob@example.com");
        request.setAmount(new BigDecimal("10.00"));
        request.setCurrency("USD");
        request.setGatewayType(GatewayType.PAYTM);
    }

    @Test
    void processPayment_returnsExistingWhenIdempotencyKeyPresent() {
        String idempotencyKey = "idem-123";
        Transaction existing = new Transaction();
        existing.setId(42L);
        existing.setSender(request.getSender());
        existing.setReceiver(request.getReceiver());
        existing.setAmount(request.getAmount());
        existing.setCurrency(request.getCurrency());
        existing.setStatus("SUCCESS_PAYTM");
        existing.setCreatedAt(Instant.now());
        existing.setIdempotencyKey(idempotencyKey);

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existing));

        Transaction result = paymentService.processPayment(request, idempotencyKey);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(42L);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void processPayment_createsAndSavesWhenNoExistingIdempotency() {
        String idempotencyKey = "idem-456";

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(gatewayFactory.getGateway(request.getGatewayType())).thenReturn(paymentGateway);

        Transaction gatewayResult = new Transaction();
        gatewayResult.setSender(request.getSender());
        gatewayResult.setReceiver(request.getReceiver());
        gatewayResult.setAmount(request.getAmount());
        gatewayResult.setCurrency(request.getCurrency());
        gatewayResult.setStatus("SUCCESS_PAYTM");
        gatewayResult.setCreatedAt(Instant.now());

        when(proxy.processPayment(request, paymentGateway)).thenReturn(gatewayResult);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(100L);
            return t;
        });

        Transaction result = paymentService.processPayment(request, idempotencyKey);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        verify(transactionRepository).save(txCaptor.capture());
        Transaction saved = txCaptor.getValue();
        assertThat(saved.getIdempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(saved.getStatus()).isEqualTo("SUCCESS_PAYTM");
    }
}

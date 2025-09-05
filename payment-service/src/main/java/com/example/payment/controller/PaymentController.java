package com.example.payment.controller;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.payment.model.PaymentRequest;
import com.example.payment.model.Transaction;
import com.example.payment.model.TransactionDto;
import com.example.payment.service.PaymentService;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Operations related to payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a payment", description = "Processes a payment request and returns the created transaction")
    @ApiResponse(responseCode = "201", description = "Payment created")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public ResponseEntity<TransactionDto> createPayment(@Valid @RequestBody PaymentRequest request,
                                                        @RequestHeader(value = "Idempotency-Key", required = false)
                                                        @Parameter(name = "Idempotency-Key", in = ParameterIn.HEADER, description = "Idempotency key to prevent duplicate processing", required = false)
                                                        String idempotencyKey) {
        log.info("Create payment request received for sender={}, idempotencyKey={}", request.getSender(), idempotencyKey);
        Transaction tx = paymentService.processPayment(request, idempotencyKey);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(tx.getId()).toUri();
        TransactionDto dto = TransactionDto.from(tx);
        return ResponseEntity.created(location).body(dto);
    }
}

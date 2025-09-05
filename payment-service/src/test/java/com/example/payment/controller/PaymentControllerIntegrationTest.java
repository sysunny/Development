package com.example.payment.controller;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.payment.model.PaymentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void postPayment_returnsCreatedAndHonorsIdempotency() throws Exception {
        PaymentRequest req = new PaymentRequest();
        req.setSender("int-alice@example.com");
        req.setReceiver("int-bob@example.com");
        req.setAmount(new BigDecimal("11.00"));
        req.setCurrency("USD");
        // gatewayType omitted to use default

        String json = objectMapper.writeValueAsString(req);
        String idempotencyKey = "int-idem-1";

        MvcResult r1 = mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", idempotencyKey)
                .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        String body1 = r1.getResponse().getContentAsString();
        assertThat(body1).contains("id");

        MvcResult r2 = mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", idempotencyKey)
                .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        String body2 = r2.getResponse().getContentAsString();
        assertThat(body2).isEqualTo(body1);
    }
}

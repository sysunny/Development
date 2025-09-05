package com.example.payment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.payment.model.PaymentRequest;
import com.example.payment.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentConcurrencyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void concurrentRequests_withSameIdempotencyKey_resultInSingleTransaction() throws Exception {
        transactionRepository.deleteAll();

        PaymentRequest req = new PaymentRequest();
        req.setSender("concurrent-alice@example.com");
        req.setReceiver("concurrent-bob@example.com");
        req.setAmount(new BigDecimal("13.00"));
        req.setCurrency("USD");

        String json = objectMapper.writeValueAsString(req);
        String idempotencyKey = "concurrent-idem-1";

        int threads = 10;
        ExecutorService ex = Executors.newFixedThreadPool(threads);
        List<Callable<String>> tasks = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            tasks.add(() -> mockMvc.perform(post("/api/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Idempotency-Key", idempotencyKey)
                    .content(json))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString());
        }

        List<Future<String>> futures = ex.invokeAll(tasks);
        List<String> responses = new ArrayList<>();
        for (Future<String> f : futures) {
            responses.add(f.get());
        }
        ex.shutdown();

        // All responses should be identical (same persisted transaction)
        for (String r : responses) {
            assertThat(r).isEqualTo(responses.get(0));
        }

        // Exactly one transaction with the idempotency key should exist
        long countWithKey = transactionRepository.findAll().stream().filter(t -> idempotencyKey.equals(t.getIdempotencyKey())).count();
        assertThat(countWithKey).isEqualTo(1);
    }
}

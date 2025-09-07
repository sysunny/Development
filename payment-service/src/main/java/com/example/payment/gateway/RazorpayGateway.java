package com.example.payment.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.payment.banking.RazorpayBankingSystem;
import com.example.payment.model.PaymentRequest;
import com.example.payment.model.Transaction;

@Component("razorpayGateway")
public class RazorpayGateway implements PaymentGateway {

    private final RazorpayBankingSystem razorpayBankingSystem;
    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String apiKey;

    public RazorpayGateway(RazorpayBankingSystem razorpayBankingSystem, RestTemplateBuilder restTemplateBuilder,
                          @Value("${gateways.razorpay.url:http://localhost:8082/razorpay/process}") String apiUrl,
                          @Value("${gateways.razorpay.apiKey:}") String apiKey) {
        this.razorpayBankingSystem = razorpayBankingSystem;
        this.restTemplate = restTemplateBuilder.build();
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

    @Override
    public Transaction processPayment(PaymentRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (StringUtils.hasText(apiKey)) {
                headers.set("Authorization", "Bearer " + apiKey);
            }
            HttpEntity<PaymentRequest> entity = new HttpEntity<>(request, headers);
            ResponseEntity<Transaction> resp = restTemplate.postForEntity(apiUrl, entity, Transaction.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                return resp.getBody();
            }
        } catch (RestClientException ex) {
            // Remote call failed - fallback to local banking simulation
        }
        return razorpayBankingSystem.process(request);
    }
}

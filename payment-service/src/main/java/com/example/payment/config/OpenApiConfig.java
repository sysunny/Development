package com.example.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI paymentOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Payment Service API")
                        .version("0.0.1")
                        .description("API for processing payments, with idempotency support"));
    }

    // Removed OpenApiCustomiser bean due to runtime classpath issues with org.springdoc.core.customizers.OpenApiCustomiser.
}

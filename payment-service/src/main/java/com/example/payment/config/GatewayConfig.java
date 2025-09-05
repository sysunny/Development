package com.example.payment.config;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.payment.gateway.PaymentGateway;
import com.example.payment.model.GatewayType;

/**
 * Configuration that maps PaymentGateway beans to their corresponding GatewayType enum.
 */
@Configuration
public class GatewayConfig {

    private static final Logger log = LoggerFactory.getLogger(GatewayConfig.class);

    /**
     * Create a map from GatewayType to PaymentGateway by examining bean names.
     *
     * Bean names ending with 'Gateway' will have that suffix removed, non-letter
     * characters stripped, and then uppercased to match GatewayType enum values
     * (for example, 'paytmGateway' -> PAYTM).
     *
     * @param gateways a map of Spring bean names to PaymentGateway instances
     * @return an EnumMap mapping GatewayType to the corresponding PaymentGateway
     */
    @Bean
    public Map<GatewayType, PaymentGateway> paymentGatewayMap(Map<String, PaymentGateway> gateways) {
        Map<GatewayType, PaymentGateway> result = new EnumMap<>(GatewayType.class);
        for (Entry<String, PaymentGateway> e : gateways.entrySet()) {
            String beanName = e.getKey();
            PaymentGateway gateway = e.getValue();

            String base = beanName;
            if (base.endsWith("Gateway")) {
                base = base.substring(0, base.length() - "Gateway".length());
            }
            // Normalize: remove non-letters and uppercase to match enum names like PAYTM, RAZORPAY
            String enumName = base.replaceAll("[^A-Za-z]", "").toUpperCase();
            try {
                GatewayType type = GatewayType.valueOf(enumName);
                if (result.put(type, gateway) != null) {
                    log.warn("Multiple gateways mapped for type {} - overriding with bean {}", type, beanName);
                } else {
                    log.info("Mapped gateway bean '{}' to type {}", beanName, type);
                }
            } catch (IllegalArgumentException ex) {
                log.warn("Skipping gateway bean '{}' - cannot map to GatewayType (derived name='{}')", beanName, enumName);
            }
        }
        log.info("Registered gateway types: {}", result.keySet());
        return result;
    }
}

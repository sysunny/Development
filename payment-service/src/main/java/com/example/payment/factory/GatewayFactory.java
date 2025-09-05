package com.example.payment.factory;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.payment.gateway.PaymentGateway;
import com.example.payment.model.GatewayType;

@Component
public class GatewayFactory {

    private static final Logger log = LoggerFactory.getLogger(GatewayFactory.class);

    private final Map<GatewayType, PaymentGateway> gateways;

    public GatewayFactory(Map<GatewayType, PaymentGateway> gateways) {
        this.gateways = gateways;
    }

    public PaymentGateway getGateway(GatewayType type) {
        if (type == null) {
            type = GatewayType.PAYTM; // default
        }
        PaymentGateway gateway = gateways.get(type);
        if (gateway == null) {
            log.error("No gateway found for type {}. Available types: {}", type, gateways.keySet());
            throw new IllegalArgumentException("No gateway found for type: " + type);
        }
        return gateway;
    }
}

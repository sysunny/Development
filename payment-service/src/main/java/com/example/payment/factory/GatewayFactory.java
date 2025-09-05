package com.example.payment.factory;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.payment.gateway.PaymentGateway;
import com.example.payment.model.GatewayType;

@Component
public class GatewayFactory {

    private final Map<String, PaymentGateway> gateways;

    public GatewayFactory(Map<String, PaymentGateway> gateways) {
        this.gateways = gateways;
    }

    public PaymentGateway getGateway(GatewayType type) {
        if (type == null) {
            type = GatewayType.PAYTM; // default
        }
        // Bean names are registered in lowerCamelCase by Spring (e.g. paytmGateway)
        String beanName = type.name().toLowerCase() + "Gateway";
        PaymentGateway gateway = gateways.get(beanName);
        if (gateway == null) {
            throw new IllegalArgumentException("No gateway found for type: " + type);
        }
        return gateway;
    }
}

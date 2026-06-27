package com.wellnesspro.gateway;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Dev/test payment gateway: always approves and returns a synthetic reference, mirroring
 * the app's historical always-COMPLETED behavior. {@code @Primary} so a future real impl
 * (e.g. Stripe, gated by profile) can be added without an ambiguous-bean conflict here.
 */
@Component
@Primary
public class SimulatedPaymentGateway implements PaymentGateway {

    @Override
    public PaymentResult charge(Long memberId, BigDecimal amount, String method, String description) {
        return PaymentResult.success("SIM-" + UUID.randomUUID());
    }
}

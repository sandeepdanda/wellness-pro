package com.wellnesspro.gateway;

import java.math.BigDecimal;

/**
 * The seam between this app and a payment processor. Subscription and renewal flows
 * depend on this interface, not on any concrete processor. {@link SimulatedPaymentGateway}
 * is the dev/test impl; a real Stripe-backed impl can be dropped in later behind the same
 * contract without touching callers.
 */
public interface PaymentGateway {

    /**
     * Attempt to charge {@code amount} to the member's payment method.
     *
     * @return a {@link PaymentResult} describing success (with a gateway reference) or
     *         failure (with a reason). Never throws for an ordinary decline.
     */
    PaymentResult charge(Long memberId, BigDecimal amount, String method, String description);
}

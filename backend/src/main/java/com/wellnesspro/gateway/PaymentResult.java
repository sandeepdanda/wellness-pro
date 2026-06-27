package com.wellnesspro.gateway;

/**
 * Outcome of a charge attempt against a {@link PaymentGateway}. {@code reference} is the
 * gateway's transaction id on success; {@code failureReason} explains a decline. Exactly
 * one of the two is meaningful depending on {@code success}.
 */
public record PaymentResult(boolean success, String reference, String failureReason) {

    public static PaymentResult success(String reference) {
        return new PaymentResult(true, reference, null);
    }

    public static PaymentResult failure(String failureReason) {
        return new PaymentResult(false, null, failureReason);
    }
}

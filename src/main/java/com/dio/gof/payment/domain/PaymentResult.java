package com.dio.gof.payment.domain;

import java.math.BigDecimal;

public record PaymentResult(
        String transactionId,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        BigDecimal originalAmount,
        BigDecimal finalAmount,
        BigDecimal discountOrFee,
        String message
) {
}

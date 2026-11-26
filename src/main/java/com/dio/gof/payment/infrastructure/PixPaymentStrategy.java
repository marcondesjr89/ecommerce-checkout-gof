package com.dio.gof.payment.infrastructure;

import com.dio.gof.payment.domain.PaymentMethod;
import com.dio.gof.payment.domain.PaymentResult;
import com.dio.gof.payment.domain.PaymentStatus;
import com.dio.gof.payment.domain.PaymentStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * GoF Pattern: Strategy (Concrete Implementation - PIX)
 *
 * Applies a 10% instant payment discount and settles the transaction immediately.
 */
@Component
public class PixPaymentStrategy implements PaymentStrategy {

    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.PIX;
    }

    @Override
    public PaymentResult process(BigDecimal amount, String customerEmail) {
        BigDecimal normalizedAmount = amount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = normalizedAmount.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalAmount = normalizedAmount.subtract(discount);
        String transactionId = "PIX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return new PaymentResult(
                transactionId,
                PaymentMethod.PIX,
                PaymentStatus.COMPLETED,
                normalizedAmount,
                finalAmount,
                discount.negate(),
                "Pagamento via PIX aprovado com 10% de desconto."
        );
    }
}

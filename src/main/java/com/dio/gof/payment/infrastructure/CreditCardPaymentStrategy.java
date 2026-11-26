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
 * GoF Pattern: Strategy (Concrete Implementation - Credit Card)
 *
 * Applies a 2.5% card processing fee and authorizes the transaction immediately.
 */
@Component
public class CreditCardPaymentStrategy implements PaymentStrategy {

    private static final BigDecimal PROCESSING_FEE_RATE = new BigDecimal("0.025");

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.CREDIT_CARD;
    }

    @Override
    public PaymentResult process(BigDecimal amount, String customerEmail) {
        BigDecimal normalizedAmount = amount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal fee = normalizedAmount.multiply(PROCESSING_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalAmount = normalizedAmount.add(fee);
        String transactionId = "CARD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return new PaymentResult(
                transactionId,
                PaymentMethod.CREDIT_CARD,
                PaymentStatus.COMPLETED,
                normalizedAmount,
                finalAmount,
                fee,
                "Transação de Cartão de Crédito autorizada pela operadora."
        );
    }
}

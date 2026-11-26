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
 * GoF Pattern: Strategy (Concrete Implementation - Boleto)
 *
 * Issues a bank slip at full price with zero extra fee and PENDING settlement status.
 */
@Component
public class BoletoPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.BOLETO;
    }

    @Override
    public PaymentResult process(BigDecimal amount, String customerEmail) {
        BigDecimal normalizedAmount = amount.setScale(2, RoundingMode.HALF_UP);
        String transactionId = "BOL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return new PaymentResult(
                transactionId,
                PaymentMethod.BOLETO,
                PaymentStatus.PENDING,
                normalizedAmount,
                normalizedAmount,
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                "Boleto bancário emitido. Vencimento em 3 dias úteis."
        );
    }
}

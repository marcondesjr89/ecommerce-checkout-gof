package com.dio.gof.payment.application;

import com.dio.gof.payment.domain.PaymentMethod;
import com.dio.gof.payment.domain.PaymentResult;
import com.dio.gof.payment.domain.PaymentStrategy;
import com.dio.gof.platform.api.BusinessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentService {

    private final PaymentStrategyResolver strategyResolver;

    public PaymentService(PaymentStrategyResolver strategyResolver) {
        this.strategyResolver = strategyResolver;
    }

    public PaymentResult executePayment(PaymentMethod method, BigDecimal amount, String customerEmail) {
        validatePaymentInputs(amount, customerEmail);
        PaymentStrategy strategy = strategyResolver.resolve(method);
        return strategy.process(amount, customerEmail);
    }

    private void validatePaymentInputs(BigDecimal amount, String customerEmail) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("O valor do pagamento deve ser maior que zero.");
        }
        if (customerEmail == null || customerEmail.isBlank()) {
            throw new BusinessException("O e-mail do cliente é obrigatório para processar o pagamento.");
        }
    }
}

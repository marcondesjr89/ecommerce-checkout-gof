package com.dio.gof.payment.application;

import com.dio.gof.payment.domain.PaymentMethod;
import com.dio.gof.payment.domain.PaymentStrategy;
import com.dio.gof.platform.api.BusinessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * GoF Pattern: Strategy (Dynamic Polymorphic Resolver)
 *
 * Leverages Spring IoC to inject all PaymentStrategy beans into an immutable lookup map,
 * eliminating conditional branching (if/switch) and adhering to the Open-Closed Principle (OCP).
 */
@Component
public class PaymentStrategyResolver {

    private final Map<PaymentMethod, PaymentStrategy> strategies;

    public PaymentStrategyResolver(List<PaymentStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toUnmodifiableMap(PaymentStrategy::getMethod, Function.identity()));
    }

    public PaymentStrategy resolve(PaymentMethod method) {
        if (method == null) {
            throw new BusinessException("A forma de pagamento é obrigatória.");
        }
        return Optional.ofNullable(strategies.get(method))
                .orElseThrow(() -> new BusinessException("Forma de pagamento não suportada: " + method));
    }
}

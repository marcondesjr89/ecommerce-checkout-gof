package com.dio.gof.orders.application;

import com.dio.gof.orders.domain.ShippingMethod;
import com.dio.gof.orders.domain.ShippingStrategy;
import com.dio.gof.platform.api.BusinessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * GoF Pattern: Strategy (Dynamic Shipping Strategy Resolver)
 *
 * Injects all ShippingStrategy beans managed by Spring IoC into an immutable lookup map.
 */
@Component
public class ShippingStrategyResolver {

    private final Map<ShippingMethod, ShippingStrategy> strategies;

    public ShippingStrategyResolver(List<ShippingStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toUnmodifiableMap(ShippingStrategy::getMethod, Function.identity()));
    }

    public ShippingStrategy resolve(ShippingMethod method) {
        if (method == null) {
            throw new BusinessException("O método de frete é obrigatório.");
        }
        return Optional.ofNullable(strategies.get(method))
                .orElseThrow(() -> new BusinessException("Método de frete não suportado: " + method));
    }
}

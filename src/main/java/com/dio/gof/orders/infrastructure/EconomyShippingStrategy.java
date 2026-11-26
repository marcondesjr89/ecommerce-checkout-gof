package com.dio.gof.orders.infrastructure;

import com.dio.gof.orders.domain.ShippingCalculation;
import com.dio.gof.orders.domain.ShippingMethod;
import com.dio.gof.orders.domain.ShippingStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * GoF Pattern: Strategy (Concrete Implementation - Economy Shipping)
 *
 * Standard fee of R$ 15.00 with free shipping for orders of R$ 200.00 or more.
 * Estimated delivery in 5 business days.
 */
@Component
public class EconomyShippingStrategy implements ShippingStrategy {

    private static final BigDecimal STANDARD_ECONOMY_COST = new BigDecimal("15.00");
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("200.00");
    private static final int ESTIMATED_DELIVERY_DAYS = 5;

    @Override
    public ShippingMethod getMethod() {
        return ShippingMethod.ECONOMY;
    }

    @Override
    public ShippingCalculation calculate(BigDecimal orderSubtotal, String postalCode) {
        boolean qualifiesForFreeShipping = orderSubtotal != null
                && orderSubtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0;

        BigDecimal cost = qualifiesForFreeShipping
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : STANDARD_ECONOMY_COST;

        String description = qualifiesForFreeShipping
                ? "Frete Econômico Grátis (pedidos a partir de R$ 200,00): 5 dias úteis."
                : "Frete Econômico padrão: 5 dias úteis.";

        return new ShippingCalculation(
                ShippingMethod.ECONOMY,
                cost,
                ESTIMATED_DELIVERY_DAYS,
                description
        );
    }
}

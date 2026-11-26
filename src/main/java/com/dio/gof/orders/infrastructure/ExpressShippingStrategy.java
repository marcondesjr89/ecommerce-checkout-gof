package com.dio.gof.orders.infrastructure;

import com.dio.gof.orders.domain.ShippingCalculation;
import com.dio.gof.orders.domain.ShippingMethod;
import com.dio.gof.orders.domain.ShippingStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * GoF Pattern: Strategy (Concrete Implementation - Express Shipping)
 *
 * Priority delivery in 1 business day for a fixed cost of R$ 35.00.
 */
@Component
public class ExpressShippingStrategy implements ShippingStrategy {

    private static final BigDecimal FIXED_EXPRESS_COST = new BigDecimal("35.00");
    private static final int ESTIMATED_DELIVERY_DAYS = 1;

    @Override
    public ShippingMethod getMethod() {
        return ShippingMethod.EXPRESS;
    }

    @Override
    public ShippingCalculation calculate(BigDecimal orderSubtotal, String postalCode) {
        return new ShippingCalculation(
                ShippingMethod.EXPRESS,
                FIXED_EXPRESS_COST,
                ESTIMATED_DELIVERY_DAYS,
                "Frete Expresso: entrega prioritária em até 1 dia útil."
        );
    }
}

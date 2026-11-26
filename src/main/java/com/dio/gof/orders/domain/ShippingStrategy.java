package com.dio.gof.orders.domain;

import java.math.BigDecimal;

/**
 * GoF Pattern: Strategy (Behavioral)
 *
 * Defines the contract for interchangeable shipping cost calculation algorithms.
 */
public interface ShippingStrategy {

    ShippingMethod getMethod();

    ShippingCalculation calculate(BigDecimal orderSubtotal, String postalCode);
}

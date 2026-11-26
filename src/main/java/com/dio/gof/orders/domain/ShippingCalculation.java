package com.dio.gof.orders.domain;

import java.math.BigDecimal;

public record ShippingCalculation(
        ShippingMethod method,
        BigDecimal cost,
        int estimatedDeliveryDays,
        String description
) {
}

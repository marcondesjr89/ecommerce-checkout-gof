package com.dio.gof.orders.infrastructure;

import com.dio.gof.orders.domain.ShippingCalculation;
import com.dio.gof.orders.domain.ShippingMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GoF Strategy Pattern - Concrete Shipping Strategies Tests")
class ShippingStrategyTest {

    @Test
    @DisplayName("ExpressShippingStrategy should charge fixed 35.00 with 1 business day delivery")
    void shouldReturnFixedCostForExpressShipping() {
        ExpressShippingStrategy strategy = new ExpressShippingStrategy();

        ShippingCalculation result = strategy.calculate(new BigDecimal("150.00"), "01001-000");

        assertThat(strategy.getMethod()).isEqualTo(ShippingMethod.EXPRESS);
        assertThat(result.method()).isEqualTo(ShippingMethod.EXPRESS);
        assertThat(result.cost()).isEqualByComparingTo("35.00");
        assertThat(result.estimatedDeliveryDays()).isEqualTo(1);
    }

    @Test
    @DisplayName("EconomyShippingStrategy should charge 15.00 for orders below 200.00")
    void shouldChargeStandardEconomyCostBelowThreshold() {
        EconomyShippingStrategy strategy = new EconomyShippingStrategy();

        ShippingCalculation result = strategy.calculate(new BigDecimal("199.99"), "01001-000");

        assertThat(strategy.getMethod()).isEqualTo(ShippingMethod.ECONOMY);
        assertThat(result.method()).isEqualTo(ShippingMethod.ECONOMY);
        assertThat(result.cost()).isEqualByComparingTo("15.00");
        assertThat(result.estimatedDeliveryDays()).isEqualTo(5);
    }

    @Test
    @DisplayName("EconomyShippingStrategy should grant free shipping for orders of 200.00 or more")
    void shouldGrantFreeShippingAtOrAboveThreshold() {
        EconomyShippingStrategy strategy = new EconomyShippingStrategy();

        ShippingCalculation exactThreshold = strategy.calculate(new BigDecimal("200.00"), "01001-000");
        ShippingCalculation aboveThreshold = strategy.calculate(new BigDecimal("350.00"), "01001-000");

        assertThat(exactThreshold.cost()).isEqualByComparingTo("0.00");
        assertThat(aboveThreshold.cost()).isEqualByComparingTo("0.00");
        assertThat(aboveThreshold.estimatedDeliveryDays()).isEqualTo(5);
    }

    @Test
    @DisplayName("EconomyShippingStrategy should charge standard 15.00 when subtotal is null")
    void shouldChargeStandardEconomyCostWhenSubtotalIsNull() {
        EconomyShippingStrategy strategy = new EconomyShippingStrategy();

        ShippingCalculation result = strategy.calculate(null, "01001-000");

        assertThat(result.cost()).isEqualByComparingTo("15.00");
        assertThat(result.estimatedDeliveryDays()).isEqualTo(5);
    }
}

package com.dio.gof.orders.application;

import com.dio.gof.orders.domain.ShippingMethod;
import com.dio.gof.orders.infrastructure.EconomyShippingStrategy;
import com.dio.gof.orders.infrastructure.ExpressShippingStrategy;
import com.dio.gof.platform.api.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayName("GoF Strategy Pattern - ShippingStrategyResolver Tests")
class ShippingStrategyResolverTest {

    @Autowired
    private ShippingStrategyResolver resolver;

    @Test
    @DisplayName("Should resolve all shipping strategies injected automatically by Spring IoC")
    void shouldResolveAllRegisteredShippingStrategies() {
        assertThat(resolver.resolve(ShippingMethod.EXPRESS)).isInstanceOf(ExpressShippingStrategy.class);
        assertThat(resolver.resolve(ShippingMethod.ECONOMY)).isInstanceOf(EconomyShippingStrategy.class);
    }

    @Test
    @DisplayName("Should throw BusinessException when shipping method is null")
    void shouldThrowBusinessExceptionWhenMethodIsNull() {
        assertThatThrownBy(() -> resolver.resolve(null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("obrigatório");
    }

    @Test
    @DisplayName("Should throw BusinessException when no strategy is registered for the requested shipping method")
    void shouldThrowBusinessExceptionWhenStrategyIsMissing() {
        ShippingStrategyResolver partialResolver = new ShippingStrategyResolver(List.of(new ExpressShippingStrategy()));

        assertThat(partialResolver.resolve(ShippingMethod.EXPRESS)).isInstanceOf(ExpressShippingStrategy.class);

        assertThatThrownBy(() -> partialResolver.resolve(ShippingMethod.ECONOMY))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ECONOMY");
    }
}

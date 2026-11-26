package com.dio.gof.payment.application;

import com.dio.gof.payment.domain.PaymentMethod;
import com.dio.gof.payment.domain.PaymentStrategy;
import com.dio.gof.payment.infrastructure.BoletoPaymentStrategy;
import com.dio.gof.payment.infrastructure.CreditCardPaymentStrategy;
import com.dio.gof.payment.infrastructure.PixPaymentStrategy;
import com.dio.gof.platform.api.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayName("GoF Strategy Pattern - PaymentStrategyResolver Tests")
class PaymentStrategyResolverTest {

    @Autowired
    private PaymentStrategyResolver resolver;

    @Test
    @DisplayName("Should resolve all payment strategies injected automatically by Spring IoC")
    void shouldResolveAllRegisteredStrategies() {
        assertThat(resolver.resolve(PaymentMethod.PIX)).isInstanceOf(PixPaymentStrategy.class);
        assertThat(resolver.resolve(PaymentMethod.CREDIT_CARD)).isInstanceOf(CreditCardPaymentStrategy.class);
        assertThat(resolver.resolve(PaymentMethod.BOLETO)).isInstanceOf(BoletoPaymentStrategy.class);
    }

    @Test
    @DisplayName("Should throw BusinessException when payment method is null")
    void shouldThrowBusinessExceptionWhenMethodIsNull() {
        assertThatThrownBy(() -> resolver.resolve(null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("obrigatória");
    }

    @Test
    @DisplayName("Should throw BusinessException when no strategy is registered for the requested method")
    void shouldThrowBusinessExceptionWhenStrategyIsMissing() {
        PaymentStrategyResolver partialResolver = new PaymentStrategyResolver(List.of(new PixPaymentStrategy()));

        PaymentStrategy resolved = partialResolver.resolve(PaymentMethod.PIX);
        assertThat(resolved).isInstanceOf(PixPaymentStrategy.class);

        assertThatThrownBy(() -> partialResolver.resolve(PaymentMethod.BOLETO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("BOLETO");
    }
}

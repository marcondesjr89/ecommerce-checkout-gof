package com.dio.gof.payment.application;

import com.dio.gof.payment.domain.PaymentMethod;
import com.dio.gof.payment.domain.PaymentResult;
import com.dio.gof.payment.domain.PaymentStatus;
import com.dio.gof.payment.infrastructure.BoletoPaymentStrategy;
import com.dio.gof.payment.infrastructure.CreditCardPaymentStrategy;
import com.dio.gof.payment.infrastructure.PixPaymentStrategy;
import com.dio.gof.platform.api.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PaymentService Application Layer Tests")
class PaymentServiceTest {

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        PaymentStrategyResolver resolver = new PaymentStrategyResolver(List.of(
                new PixPaymentStrategy(),
                new CreditCardPaymentStrategy(),
                new BoletoPaymentStrategy()
        ));
        paymentService = new PaymentService(resolver);
    }

    @Test
    @DisplayName("Should delegate payment execution to the resolved strategy")
    void shouldExecutePaymentViaResolvedStrategy() {
        PaymentResult result = paymentService.executePayment(
                PaymentMethod.PIX,
                new BigDecimal("100.00"),
                "buyer@example.com"
        );

        assertThat(result.status()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(result.finalAmount()).isEqualByComparingTo("90.00");
    }

    @Test
    @DisplayName("Should reject null, zero, or negative payment amounts")
    void shouldRejectInvalidPaymentAmounts() {
        assertThatThrownBy(() -> paymentService.executePayment(PaymentMethod.PIX, null, "buyer@example.com"))
                .isInstanceOf(BusinessException.class);

        assertThatThrownBy(() -> paymentService.executePayment(PaymentMethod.PIX, BigDecimal.ZERO, "buyer@example.com"))
                .isInstanceOf(BusinessException.class);

        assertThatThrownBy(() -> paymentService.executePayment(PaymentMethod.PIX, new BigDecimal("-10.00"), "buyer@example.com"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Should reject blank customer email")
    void shouldRejectBlankCustomerEmail() {
        assertThatThrownBy(() -> paymentService.executePayment(PaymentMethod.BOLETO, new BigDecimal("50.00"), "  "))
                .isInstanceOf(BusinessException.class);
    }
}

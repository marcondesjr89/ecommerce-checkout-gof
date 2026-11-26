package com.dio.gof.payment.infrastructure;

import com.dio.gof.payment.domain.PaymentMethod;
import com.dio.gof.payment.domain.PaymentResult;
import com.dio.gof.payment.domain.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GoF Strategy Pattern - Concrete Payment Strategies Tests")
class PaymentStrategyTest {

    @Test
    @DisplayName("PixPaymentStrategy should apply 10% discount and return COMPLETED status")
    void shouldApplyTenPercentDiscountForPix() {
        PixPaymentStrategy strategy = new PixPaymentStrategy();
        BigDecimal originalAmount = new BigDecimal("100.00");

        PaymentResult result = strategy.process(originalAmount, "customer@example.com");

        assertThat(strategy.getMethod()).isEqualTo(PaymentMethod.PIX);
        assertThat(result.paymentMethod()).isEqualTo(PaymentMethod.PIX);
        assertThat(result.status()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(result.originalAmount()).isEqualByComparingTo("100.00");
        assertThat(result.discountOrFee()).isEqualByComparingTo("-10.00");
        assertThat(result.finalAmount()).isEqualByComparingTo("90.00");
        assertThat(result.transactionId()).startsWith("PIX-");
    }

    @Test
    @DisplayName("CreditCardPaymentStrategy should apply 2.5% fee with HALF_UP rounding and COMPLETED status")
    void shouldApplyProcessingFeeForCreditCard() {
        CreditCardPaymentStrategy strategy = new CreditCardPaymentStrategy();
        BigDecimal originalAmount = new BigDecimal("200.00");

        PaymentResult result = strategy.process(originalAmount, "customer@example.com");

        assertThat(strategy.getMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(result.paymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(result.status()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(result.originalAmount()).isEqualByComparingTo("200.00");
        assertThat(result.discountOrFee()).isEqualByComparingTo("5.00");
        assertThat(result.finalAmount()).isEqualByComparingTo("205.00");
        assertThat(result.transactionId()).startsWith("CARD-");
    }

    @Test
    @DisplayName("BoletoPaymentStrategy should keep full amount with zero fee and PENDING status")
    void shouldKeepFullAmountAndPendingStatusForBoleto() {
        BoletoPaymentStrategy strategy = new BoletoPaymentStrategy();
        BigDecimal originalAmount = new BigDecimal("150.00");

        PaymentResult result = strategy.process(originalAmount, "customer@example.com");

        assertThat(strategy.getMethod()).isEqualTo(PaymentMethod.BOLETO);
        assertThat(result.paymentMethod()).isEqualTo(PaymentMethod.BOLETO);
        assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.originalAmount()).isEqualByComparingTo("150.00");
        assertThat(result.discountOrFee()).isEqualByComparingTo("0.00");
        assertThat(result.finalAmount()).isEqualByComparingTo("150.00");
        assertThat(result.transactionId()).startsWith("BOL-");
    }

    @Test
    @DisplayName("Should round fractional cents using HALF_UP for PIX and Credit Card")
    void shouldRoundFractionalCentsUsingHalfUp() {
        PixPaymentStrategy pixStrategy = new PixPaymentStrategy();
        PaymentResult pixResult = pixStrategy.process(new BigDecimal("33.33"), "customer@example.com");
        assertThat(pixResult.discountOrFee()).isEqualByComparingTo("-3.33");
        assertThat(pixResult.finalAmount()).isEqualByComparingTo("30.00");

        CreditCardPaymentStrategy cardStrategy = new CreditCardPaymentStrategy();
        PaymentResult cardResult = cardStrategy.process(new BigDecimal("10.20"), "customer@example.com");
        assertThat(cardResult.discountOrFee()).isEqualByComparingTo("0.26");
        assertThat(cardResult.finalAmount()).isEqualByComparingTo("10.46");
    }
}

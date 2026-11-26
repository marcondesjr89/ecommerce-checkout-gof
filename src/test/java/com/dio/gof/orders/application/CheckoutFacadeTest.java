package com.dio.gof.orders.application;

import com.dio.gof.orders.api.CheckoutRequest;
import com.dio.gof.orders.api.CheckoutResponse;
import com.dio.gof.orders.api.OrderItemRequest;
import com.dio.gof.orders.domain.Order;
import com.dio.gof.orders.domain.OrderStatus;
import com.dio.gof.orders.domain.ShippingCalculation;
import com.dio.gof.orders.domain.ShippingMethod;
import com.dio.gof.orders.domain.ShippingStrategy;
import com.dio.gof.orders.infrastructure.OrderRepository;
import com.dio.gof.payment.application.PaymentService;
import com.dio.gof.payment.domain.PaymentMethod;
import com.dio.gof.payment.domain.PaymentResult;
import com.dio.gof.payment.domain.PaymentStatus;
import com.dio.gof.platform.api.BusinessException;
import com.dio.gof.platform.observability.AuditLogger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoF Facade Pattern - CheckoutFacade Orchestration Tests")
class CheckoutFacadeTest {

    @Mock
    private ShippingStrategyResolver shippingResolver;

    @Mock
    private PaymentService paymentService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AuditLogger auditLogger;

    @InjectMocks
    private CheckoutFacade checkoutFacade;

    @Test
    @DisplayName("Should orchestrate shipping, payment, persistence and audit for PAID PIX checkout")
    void shouldOrchestrateCompletePaidCheckout() {
        CheckoutRequest request = new CheckoutRequest(
                "Maria Oliveira",
                "maria@example.com",
                "01001-000",
                ShippingMethod.EXPRESS,
                PaymentMethod.PIX,
                List.of(new OrderItemRequest("Notebook Stand", 2, new BigDecimal("50.00")))
        );

        ShippingStrategy expressStrategy = mock(ShippingStrategy.class);
        when(shippingResolver.resolve(ShippingMethod.EXPRESS)).thenReturn(expressStrategy);
        when(expressStrategy.calculate(new BigDecimal("100.00"), "01001-000"))
                .thenReturn(new ShippingCalculation(ShippingMethod.EXPRESS, new BigDecimal("35.00"), 1, "Express"));

        when(paymentService.executePayment(PaymentMethod.PIX, new BigDecimal("135.00"), "maria@example.com"))
                .thenReturn(new PaymentResult(
                        "PIX-ABCDEF12",
                        PaymentMethod.PIX,
                        PaymentStatus.COMPLETED,
                        new BigDecimal("135.00"),
                        new BigDecimal("121.50"),
                        new BigDecimal("-13.50"),
                        "Pagamento via PIX aprovado com 10% de desconto."
                ));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CheckoutResponse response = checkoutFacade.processCheckout(request);

        assertThat(response.status()).isEqualTo(OrderStatus.PAID);
        assertThat(response.subtotal()).isEqualByComparingTo("100.00");
        assertThat(response.shippingCost()).isEqualByComparingTo("35.00");
        assertThat(response.estimatedDeliveryDays()).isEqualTo(1);
        assertThat(response.discountOrFee()).isEqualByComparingTo("-13.50");
        assertThat(response.totalAmount()).isEqualByComparingTo("121.50");
        assertThat(response.paymentTransactionId()).isEqualTo("PIX-ABCDEF12");

        verify(orderRepository).save(any(Order.class));
        verify(auditLogger).logCheckoutEvent(any(), eq("maria@example.com"), eq("PIX"), eq("EXPRESS"));
    }

    @Test
    @DisplayName("Should create PENDING_PAYMENT order when payment settles asynchronously (Boleto)")
    void shouldCreatePendingPaymentOrderForBoleto() {
        CheckoutRequest request = new CheckoutRequest(
                "Joao Santos",
                "joao@example.com",
                "20040-020",
                ShippingMethod.ECONOMY,
                PaymentMethod.BOLETO,
                List.of(new OrderItemRequest("Ergonomic Chair", 1, new BigDecimal("300.00")))
        );

        ShippingStrategy economyStrategy = mock(ShippingStrategy.class);
        when(shippingResolver.resolve(ShippingMethod.ECONOMY)).thenReturn(economyStrategy);
        when(economyStrategy.calculate(new BigDecimal("300.00"), "20040-020"))
                .thenReturn(new ShippingCalculation(ShippingMethod.ECONOMY, new BigDecimal("0.00"), 5, "Free Economy"));

        when(paymentService.executePayment(PaymentMethod.BOLETO, new BigDecimal("300.00"), "joao@example.com"))
                .thenReturn(new PaymentResult(
                        "BOL-99887766",
                        PaymentMethod.BOLETO,
                        PaymentStatus.PENDING,
                        new BigDecimal("300.00"),
                        new BigDecimal("300.00"),
                        new BigDecimal("0.00"),
                        "Boleto emitido."
                ));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CheckoutResponse response = checkoutFacade.processCheckout(request);

        assertThat(response.status()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(response.shippingCost()).isEqualByComparingTo("0.00");
        assertThat(response.totalAmount()).isEqualByComparingTo("300.00");
    }

    @Test
    @DisplayName("Should abort checkout without saving or auditing when payment fails")
    void shouldAbortCheckoutWhenPaymentFails() {
        CheckoutRequest request = new CheckoutRequest(
                "Pedro Lima",
                "pedro@example.com",
                "01001-000",
                ShippingMethod.EXPRESS,
                PaymentMethod.CREDIT_CARD,
                List.of(new OrderItemRequest("Headset", 1, new BigDecimal("100.00")))
        );

        ShippingStrategy expressStrategy = mock(ShippingStrategy.class);
        when(shippingResolver.resolve(ShippingMethod.EXPRESS)).thenReturn(expressStrategy);
        when(expressStrategy.calculate(new BigDecimal("100.00"), "01001-000"))
                .thenReturn(new ShippingCalculation(ShippingMethod.EXPRESS, new BigDecimal("35.00"), 1, "Express"));

        when(paymentService.executePayment(PaymentMethod.CREDIT_CARD, new BigDecimal("135.00"), "pedro@example.com"))
                .thenReturn(new PaymentResult(
                        "CARD-FAIL",
                        PaymentMethod.CREDIT_CARD,
                        PaymentStatus.FAILED,
                        new BigDecimal("135.00"),
                        new BigDecimal("135.00"),
                        BigDecimal.ZERO,
                        "Cartão recusado."
                ));

        assertThatThrownBy(() -> checkoutFacade.processCheckout(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cartão recusado");

        verify(orderRepository, never()).save(any());
        verify(auditLogger, never()).logCheckoutEvent(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should reject null request, null items, or empty item list")
    void shouldRejectCheckoutWithEmptyItems() {
        CheckoutRequest emptyRequest = new CheckoutRequest(
                "Maria Oliveira",
                "maria@example.com",
                "01001-000",
                ShippingMethod.EXPRESS,
                PaymentMethod.PIX,
                List.of()
        );
        CheckoutRequest nullItemsRequest = new CheckoutRequest(
                "Maria Oliveira",
                "maria@example.com",
                "01001-000",
                ShippingMethod.EXPRESS,
                PaymentMethod.PIX,
                null
        );

        assertThatThrownBy(() -> checkoutFacade.processCheckout(null))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> checkoutFacade.processCheckout(nullItemsRequest))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> checkoutFacade.processCheckout(emptyRequest))
                .isInstanceOf(BusinessException.class);

        verify(orderRepository, never()).save(any());
    }
}

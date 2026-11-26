package com.dio.gof.orders.application;

import com.dio.gof.orders.api.CheckoutRequest;
import com.dio.gof.orders.api.CheckoutResponse;
import com.dio.gof.orders.api.OrderItemRequest;
import com.dio.gof.orders.domain.Order;
import com.dio.gof.orders.domain.OrderItem;
import com.dio.gof.orders.domain.OrderStatus;
import com.dio.gof.orders.domain.ShippingCalculation;
import com.dio.gof.orders.domain.ShippingStrategy;
import com.dio.gof.orders.infrastructure.OrderRepository;
import com.dio.gof.payment.application.PaymentService;
import com.dio.gof.payment.domain.PaymentResult;
import com.dio.gof.payment.domain.PaymentStatus;
import com.dio.gof.platform.api.BusinessException;
import com.dio.gof.platform.observability.AuditLogger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * GoF Pattern: Facade (Structural)
 *
 * Provides a unified, simplified entry point over multiple checkout subsystems:
 * 1. Cart validation and items subtotal calculation.
 * 2. Shipping cost calculation via ShippingStrategy (GoF Strategy).
 * 3. Payment processing via PaymentService / PaymentStrategy (GoF Strategy).
 * 4. Transactional persistence of Order and OrderItems via OrderRepository.
 * 5. Centralized audit logging via AuditLogger (GoF Singleton).
 */
@Service
public class CheckoutFacade {

    private final ShippingStrategyResolver shippingResolver;
    private final PaymentService paymentService;
    private final OrderRepository orderRepository;
    private final AuditLogger auditLogger;

    public CheckoutFacade(ShippingStrategyResolver shippingResolver,
                          PaymentService paymentService,
                          OrderRepository orderRepository,
                          AuditLogger auditLogger) {
        this.shippingResolver = shippingResolver;
        this.paymentService = paymentService;
        this.orderRepository = orderRepository;
        this.auditLogger = auditLogger;
    }

    @Transactional
    public CheckoutResponse processCheckout(CheckoutRequest request) {
        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw new BusinessException("O checkout exige ao menos um item no carrinho.");
        }

        BigDecimal itemsSubtotal = calculateItemsSubtotal(request.items());

        ShippingStrategy shippingStrategy = shippingResolver.resolve(request.shippingMethod());
        ShippingCalculation shippingCalculation = shippingStrategy.calculate(itemsSubtotal, request.postalCode());

        BigDecimal baseAmount = itemsSubtotal.add(shippingCalculation.cost());
        PaymentResult paymentResult = paymentService.executePayment(
                request.paymentMethod(),
                baseAmount,
                request.customerEmail()
        );

        if (paymentResult.status() == PaymentStatus.FAILED) {
            throw new BusinessException("Falha na autorização do pagamento: " + paymentResult.message());
        }

        Order savedOrder = persistOrder(request, itemsSubtotal, shippingCalculation, paymentResult);

        auditLogger.logCheckoutEvent(
                savedOrder.getId(),
                savedOrder.getCustomerEmail(),
                savedOrder.getPaymentMethod().name(),
                savedOrder.getShippingMethod().name()
        );

        return new CheckoutResponse(
                savedOrder.getId(),
                savedOrder.getCustomerEmail(),
                savedOrder.getShippingMethod(),
                savedOrder.getShippingCost(),
                shippingCalculation.estimatedDeliveryDays(),
                savedOrder.getPaymentMethod(),
                savedOrder.getSubtotal(),
                savedOrder.getDiscountOrFee(),
                savedOrder.getTotalAmount(),
                savedOrder.getStatus(),
                savedOrder.getPaymentTransactionId(),
                paymentResult.message(),
                savedOrder.getCreatedAt()
        );
    }

    private BigDecimal calculateItemsSubtotal(List<OrderItemRequest> items) {
        return items.stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private Order persistOrder(CheckoutRequest request,
                               BigDecimal itemsSubtotal,
                               ShippingCalculation shippingCalculation,
                               PaymentResult paymentResult) {
        OrderStatus orderStatus = paymentResult.status() == PaymentStatus.COMPLETED
                ? OrderStatus.PAID
                : OrderStatus.PENDING_PAYMENT;

        Order order = new Order(
                request.customerName(),
                request.customerEmail(),
                request.postalCode(),
                request.shippingMethod(),
                shippingCalculation.cost(),
                request.paymentMethod(),
                itemsSubtotal,
                paymentResult.discountOrFee(),
                paymentResult.finalAmount(),
                orderStatus,
                paymentResult.transactionId()
        );

        request.items().forEach(item -> order.addItem(
                new OrderItem(item.productName(), item.quantity(), item.unitPrice())
        ));

        return orderRepository.save(order);
    }
}

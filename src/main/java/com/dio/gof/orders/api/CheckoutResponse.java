package com.dio.gof.orders.api;

import com.dio.gof.orders.domain.OrderStatus;
import com.dio.gof.orders.domain.ShippingMethod;
import com.dio.gof.payment.domain.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CheckoutResponse(
        Long orderId,
        String customerEmail,
        ShippingMethod shippingMethod,
        BigDecimal shippingCost,
        int estimatedDeliveryDays,
        PaymentMethod paymentMethod,
        BigDecimal subtotal,
        BigDecimal discountOrFee,
        BigDecimal totalAmount,
        OrderStatus status,
        String paymentTransactionId,
        String paymentMessage,
        LocalDateTime createdAt
) {
}

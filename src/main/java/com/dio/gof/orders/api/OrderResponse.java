package com.dio.gof.orders.api;

import com.dio.gof.orders.domain.Order;
import com.dio.gof.orders.domain.OrderStatus;
import com.dio.gof.orders.domain.ShippingMethod;
import com.dio.gof.payment.domain.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String customerName,
        String customerEmail,
        String postalCode,
        ShippingMethod shippingMethod,
        BigDecimal shippingCost,
        PaymentMethod paymentMethod,
        BigDecimal subtotal,
        BigDecimal discountOrFee,
        BigDecimal totalAmount,
        OrderStatus status,
        String paymentTransactionId,
        LocalDateTime createdAt,
        List<ItemResponse> items
) {
    public record ItemResponse(
            Long id,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
    }

    public static OrderResponse fromEntity(Order order) {
        List<ItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new ItemResponse(
                        item.getId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getPostalCode(),
                order.getShippingMethod(),
                order.getShippingCost(),
                order.getPaymentMethod(),
                order.getSubtotal(),
                order.getDiscountOrFee(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getPaymentTransactionId(),
                order.getCreatedAt(),
                itemResponses
        );
    }
}

package com.dio.gof.orders.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(
        name = "order_items",
        indexes = {
                @Index(name = "idx_order_items_order_id", columnList = "order_id")
        }
)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    protected OrderItem() {
    }

    public OrderItem(String productName, Integer quantity, BigDecimal unitPrice) {
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
        this.subtotal = this.unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public Order getOrder() {
        return order;
    }

    void assignOrder(Order order) {
        this.order = order;
    }
}

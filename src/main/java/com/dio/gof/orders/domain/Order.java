package com.dio.gof.orders.domain;

import com.dio.gof.payment.domain.PaymentMethod;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_orders_customer_email", columnList = "customer_email"),
                @Index(name = "idx_orders_created_at", columnList = "created_at")
        }
)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name", nullable = false, length = 150)
    private String customerName;

    @Column(name = "customer_email", nullable = false, length = 150)
    private String customerEmail;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_method", nullable = false, length = 30)
    private ShippingMethod shippingMethod;

    @Column(name = "shipping_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "discount_or_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountOrFee;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "payment_transaction_id", length = 60)
    private String paymentTransactionId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    protected Order() {
    }

    public Order(String customerName, String customerEmail, String postalCode,
                 ShippingMethod shippingMethod, BigDecimal shippingCost,
                 PaymentMethod paymentMethod, BigDecimal subtotal,
                 BigDecimal discountOrFee, BigDecimal totalAmount,
                 OrderStatus status, String paymentTransactionId) {
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.postalCode = postalCode;
        this.shippingMethod = shippingMethod;
        this.shippingCost = shippingCost;
        this.paymentMethod = paymentMethod;
        this.subtotal = subtotal;
        this.discountOrFee = discountOrFee;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentTransactionId = paymentTransactionId;
        this.createdAt = LocalDateTime.now();
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.assignOrder(this);
    }

    public Long getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public ShippingMethod getShippingMethod() {
        return shippingMethod;
    }

    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDiscountOrFee() {
        return discountOrFee;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getPaymentTransactionId() {
        return paymentTransactionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}

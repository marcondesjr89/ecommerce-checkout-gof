package com.dio.gof.orders.infrastructure;

import com.dio.gof.orders.domain.Order;
import com.dio.gof.orders.domain.OrderItem;
import com.dio.gof.orders.domain.OrderStatus;
import com.dio.gof.orders.domain.ShippingMethod;
import com.dio.gof.payment.domain.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("OrderRepository JPA Persistence Tests")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("Should persist Order with OrderItems in cascade and fetch without N+1")
    void shouldPersistOrderWithItemsAndFetchAllWithItems() {
        Order order = new Order(
                "Ana Silva",
                "ana.silva@example.com",
                "01001-000",
                ShippingMethod.EXPRESS,
                new BigDecimal("35.00"),
                PaymentMethod.PIX,
                new BigDecimal("250.00"),
                new BigDecimal("-28.50"),
                new BigDecimal("256.50"),
                OrderStatus.PAID,
                "PIX-12345678"
        );
        order.addItem(new OrderItem("Mechanical Keyboard", 1, new BigDecimal("200.00")));
        order.addItem(new OrderItem("USB-C Cable", 2, new BigDecimal("25.00")));

        Order saved = orderRepository.saveAndFlush(order);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getItems()).hasSize(2);
        assertThat(saved.getItems().get(1).getSubtotal()).isEqualByComparingTo("50.00");

        List<Order> fetchedOrders = orderRepository.findAllWithItems();
        assertThat(fetchedOrders).hasSize(1);
        assertThat(fetchedOrders.getFirst().getItems()).hasSize(2);
    }

    @Test
    @DisplayName("Should find orders by customer email ignoring case")
    void shouldFindOrdersByCustomerEmailIgnoreCase() {
        Order order = new Order(
                "Carlos Souza",
                "Carlos.Souza@Example.com",
                "20040-020",
                ShippingMethod.ECONOMY,
                BigDecimal.ZERO,
                PaymentMethod.BOLETO,
                new BigDecimal("300.00"),
                BigDecimal.ZERO,
                new BigDecimal("300.00"),
                OrderStatus.PENDING_PAYMENT,
                "BOL-87654321"
        );
        order.addItem(new OrderItem("27-inch Monitor", 1, new BigDecimal("300.00")));
        orderRepository.saveAndFlush(order);

        List<Order> results = orderRepository.findByCustomerEmailIgnoreCase("carlos.souza@example.com");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getCustomerName()).isEqualTo("Carlos Souza");
    }
}

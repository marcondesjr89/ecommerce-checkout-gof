package com.dio.gof.orders.infrastructure;

import com.dio.gof.orders.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items ORDER BY o.createdAt DESC")
    List<Order> findAllWithItems();

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(Long id);

    List<Order> findByCustomerEmailIgnoreCase(String customerEmail);
}

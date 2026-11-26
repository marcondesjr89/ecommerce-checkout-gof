package com.dio.gof.orders.application;

import com.dio.gof.orders.api.OrderResponse;
import com.dio.gof.orders.infrastructure.OrderRepository;
import com.dio.gof.platform.api.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderApplicationService {

    private final OrderRepository orderRepository;

    public OrderApplicationService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listAllOrders() {
        return orderRepository.findAllWithItems().stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findOrderById(Long id) {
        return orderRepository.findByIdWithItems(id)
                .map(OrderResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado com o ID: " + id));
    }
}

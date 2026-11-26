package com.dio.gof.orders.api;

import com.dio.gof.orders.application.OrderApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Query persisted orders and their line items")
public class OrderController {

    private final OrderApplicationService orderService;

    public OrderController(OrderApplicationService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(summary = "List all orders")
    public ResponseEntity<List<OrderResponse>> listAll() {
        return ResponseEntity.ok(orderService.listAllOrders());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find order by ID")
    public ResponseEntity<OrderResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findOrderById(id));
    }
}

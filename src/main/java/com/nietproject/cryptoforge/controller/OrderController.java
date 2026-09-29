package com.nietproject.cryptoforge.controller;

import com.nietproject.cryptoforge.dto.OrderRequest;
import com.nietproject.cryptoforge.dto.OrderResult;
import com.nietproject.cryptoforge.model.Order;
import com.nietproject.cryptoforge.repository.OrderRepository;
import com.nietproject.cryptoforge.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * Routes order placement to the raw-JDBC OrderService .
 * This is the entry point that shows Spring MVC  and JDBC  working together.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService     orderService;
    private final OrderRepository  orderRepository;

    public OrderController(OrderService orderService, OrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    /**
     * POST /api/orders
     * Places a BUY or SELL order.
     * @Valid triggers Bean Validation on OrderRequest.
     * @AuthenticationPrincipal injects the logged-in user from JWT.
     */
    @PostMapping
    public ResponseEntity<OrderResult> placeOrder(
            @Valid @RequestBody OrderRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Ensure order belongs to the authenticated user (security check)
        // In a real app you'd resolve userId from userDetails — simplified here
        return ResponseEntity.ok(orderService.placeOrder(req));
    }

    /**
     * GET /api/orders
     * Returns the authenticated user's order history.
     */
    @GetMapping
    public ResponseEntity<List<Order>> getOrderHistory(
            @RequestParam Long userId) {
        return ResponseEntity.ok(orderRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    /**
     * GET /api/orders/{id}
     * Get details of a specific order.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

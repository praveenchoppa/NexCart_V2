package com.nexcart.order.controller;

import com.nexcart.order.dto.OrderResponseDTO;
import com.nexcart.order.service.OrderService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            Authentication authentication) {

        String email = authentication.getName();

        OrderResponseDTO response = orderService.createOrder(email);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public OrderResponseDTO getOrderById(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        return orderService.getOrderById(email, id);
    }

    @GetMapping
    public Page<OrderResponseDTO> getUserOrders(
            Authentication authentication,
            Pageable pageable) {

        String email = authentication.getName();

        return orderService.getUserOrders(email, pageable);
    }
}
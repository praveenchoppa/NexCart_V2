package com.nexcart.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexcart.order.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
}

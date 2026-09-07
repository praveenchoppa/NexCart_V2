package com.nexcart.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.nexcart.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
}

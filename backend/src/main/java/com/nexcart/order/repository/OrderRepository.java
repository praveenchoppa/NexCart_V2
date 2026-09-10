package com.nexcart.order.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.nexcart.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndUserEmail(Long id, String email);

    Page<Order> findByUserEmail(String email, Pageable pageable);
}
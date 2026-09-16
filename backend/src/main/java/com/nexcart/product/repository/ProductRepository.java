package com.nexcart.product.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.nexcart.product.entity.Product;

import jakarta.persistence.LockModeType;

public interface ProductRepository extends JpaRepository<Product,Long > {
    
    boolean existsByCategoryId(Long categoryId);

    @Lock (LockModeType.PESSIMISTIC_WRITE)
    Optional<Product> findLockedById(Long id);
}

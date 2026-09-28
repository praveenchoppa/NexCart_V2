package com.nexcart.product.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.nexcart.product.entity.Product;

import jakarta.persistence.LockModeType;

public interface ProductRepository extends JpaRepository<Product,Long >, JpaSpecificationExecutor<Product> {
    
    boolean existsByCategoryId(Long categoryId);

    Page<Product> findBySellerId(Long sellerId, Pageable pageable);

    @Lock (LockModeType.PESSIMISTIC_WRITE)
    Optional<Product> findLockedById(Long id);
}

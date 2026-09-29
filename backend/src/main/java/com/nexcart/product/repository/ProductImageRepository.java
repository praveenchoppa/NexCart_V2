package com.nexcart.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexcart.product.entity.ProductImage;

public interface ProductImageRepository
        extends JpaRepository<ProductImage, Long> {
}
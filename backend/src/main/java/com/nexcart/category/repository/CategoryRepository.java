package com.nexcart.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexcart.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    boolean existsByName(String name);
}

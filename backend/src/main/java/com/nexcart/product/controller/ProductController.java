package com.nexcart.product.controller;

import org.springframework.web.bind.annotation.*;

import com.nexcart.product.dto.ProductRequestDTO;
import com.nexcart.product.dto.ProductResponseDTO;
import com.nexcart.product.service.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    
        private final ProductService productService;

        public ProductController(ProductService productService) {
            this.productService = productService;
        }

        @PostMapping
        public ProductResponseDTO createProduct(@Valid @RequestBody ProductRequestDTO request){

            return productService.createProduct(request);
        }
}

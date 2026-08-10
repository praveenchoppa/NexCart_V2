package com.nexcart.product.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
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

        @GetMapping("/{id}")
        public ProductResponseDTO getProductById(@PathVariable Long id){
            return productService.getProductById(id);
        }

        @GetMapping
        public Page<ProductResponseDTO> getAllProducts(@PageableDefault(size = 10,sort = "id")Pageable pageable){
            return productService.getAllProducts(pageable);
        } 

        @PutMapping("/{id}")
        public ProductResponseDTO updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequestDTO request){
            return productService.updateProduct(id, request);
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteProduct(@PathVariable Long id){
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        }
}

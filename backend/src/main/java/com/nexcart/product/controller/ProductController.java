package com.nexcart.product.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexcart.product.dto.ProductRequestDTO;
import com.nexcart.product.dto.ProductResponseDTO;
import com.nexcart.product.service.ProductService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ProductResponseDTO createProduct(
            @Valid @RequestBody ProductRequestDTO request,
            @AuthenticationPrincipal(expression = "username") String email) {

        return productService.createProduct(email, request);
    }

    @GetMapping("/my")
    public Page<ProductResponseDTO> getMyProducts(
            @AuthenticationPrincipal(expression = "username") String email,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        return productService.getMyProducts(email, pageable);
    }

    @GetMapping("/{id}")
    public ProductResponseDTO getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @GetMapping
    public Page<ProductResponseDTO> getAllProducts(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        return productService.getAllProducts(pageable);
    }

    @PutMapping("/{id}")
    public ProductResponseDTO updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO request,
            @AuthenticationPrincipal(expression = "username") String email) {

        return productService.updateProduct(email, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal(expression = "username") String email) {

        productService.deleteProduct(email, id);
        return ResponseEntity.noContent().build();
    }
}

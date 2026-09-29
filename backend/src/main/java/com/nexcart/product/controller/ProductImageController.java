package com.nexcart.product.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nexcart.product.dto.ProductImageResponseDTO;
import com.nexcart.product.service.ProductImageService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/products/{productId}/images")
@SecurityRequirement(name = "bearerAuth")
public class ProductImageController {

    private final ProductImageService productImageService;

    public ProductImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductImageResponseDTO addImage(
            @PathVariable Long productId,
            @RequestParam("image") MultipartFile image,
            Authentication authentication) {

        return productImageService.addImage(
                authentication.getName(),
                productId,
                image
        );
    }

    @DeleteMapping("/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteImage(
            @PathVariable Long productId,
            @PathVariable Long imageId,
            Authentication authentication) {

        productImageService.deleteImage(
                authentication.getName(),
                productId,
                imageId
        );
    }
}
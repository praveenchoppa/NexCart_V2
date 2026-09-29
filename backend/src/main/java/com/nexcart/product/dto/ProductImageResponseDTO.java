package com.nexcart.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductImageResponseDTO {

    private Long id;
    private String imageUrl;
    private Long productId;
}
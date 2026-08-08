package com.nexcart.product.dto;

import java.math.BigDecimal;

import com.nexcart.product.enums.ProductStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
    
    private Long id;

    private String name;

    private BigDecimal price;

    private String description;

    private Integer stock;

    private ProductStatus status;

    private String categoryName;
}

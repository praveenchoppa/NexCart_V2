package com.nexcart.product.dto;

import java.math.BigDecimal;

import com.nexcart.product.enums.ProductStatus;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {
    
    @NotBlank(message = "Product name is required!")
    @Size(max = 100, message = "Product name must not exceed 100 characters!")
    private String name;

    @Positive(message = "Price must be a greater than 0!")
    private BigDecimal price;

    @Size(max = 1000, message = "Description must not exceed 1000 characters!")
    private String description;

    @PositiveOrZero(message = "Stock cannot be negative!")
    private Integer stock;

    @NotNull(message = "Product status is required!")
    private ProductStatus status;

    @NotNull(message = "Category ID is required!")
    private Long categoryId;
}

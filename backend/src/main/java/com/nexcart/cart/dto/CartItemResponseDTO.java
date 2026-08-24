package com.nexcart.cart.dto;

import lombok.*;

import java.math.BigDecimal;



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponseDTO {

    private Long itemId;

    private Long productId;

    private String productName;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal subtotal;
}
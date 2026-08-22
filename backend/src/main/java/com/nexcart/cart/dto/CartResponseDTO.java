package com.nexcart.cart.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDTO {

    private Long cartId;

    private List<CartItemResponseDTO> items;

    private BigDecimal totalPrice;
}
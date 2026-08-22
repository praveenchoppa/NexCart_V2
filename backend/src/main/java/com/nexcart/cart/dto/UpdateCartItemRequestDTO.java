package com.nexcart.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCartItemRequestDTO {

    @NotNull
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
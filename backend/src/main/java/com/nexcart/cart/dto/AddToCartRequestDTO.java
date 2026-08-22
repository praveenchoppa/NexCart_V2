package com.nexcart.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequestDTO {


    @NotNull
    private Long productId;

    @NotNull
    @Min(value = 1,message = "Quantity must be at least 1")
    private Integer quantity;
}

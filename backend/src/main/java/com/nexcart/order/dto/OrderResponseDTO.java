package com.nexcart.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

    private Long orderId;

    private String status;

    private BigDecimal totalAmount;

    private LocalDateTime createdAt;

    private List<OrderItemResponseDTO> items;
}
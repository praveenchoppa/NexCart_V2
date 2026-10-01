package com.nexcart.messaging.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConversationResponseDTO {

    private Long id;

    private Long buyerId;
    private String buyerName;

    private Long sellerId;
    private String sellerName;

    private Long productId;
    private String productName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
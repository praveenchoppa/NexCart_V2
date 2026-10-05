package com.nexcart.messaging.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageResponseDTO {

    private Long id;

    private Long conversationId;

    private Long senderId;
    private String senderName;

    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
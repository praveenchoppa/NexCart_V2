package com.nexcart.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MessageCreateRequestDTO {

    @NotBlank(message = "Message content is required!")
    @Size(max = 2000, message = "Message content cannot exceed 2000 characters!")
    private String content;

    private Long conversationId;
}
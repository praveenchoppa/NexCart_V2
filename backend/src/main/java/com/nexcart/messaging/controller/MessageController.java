package com.nexcart.messaging.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.nexcart.messaging.dto.MessageCreateRequestDTO;
import com.nexcart.messaging.dto.MessageResponseDTO;
import com.nexcart.messaging.service.MessageService;

@RestController
@RequestMapping("/api/conversations/{conversationId}/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponseDTO sendMessage(
            @PathVariable Long conversationId,
            @RequestBody MessageCreateRequestDTO request,
            Authentication authentication) {

        return messageService.sendMessage(
                authentication.getName(),
                conversationId,
                request.getContent());
    }
}
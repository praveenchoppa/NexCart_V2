package com.nexcart.messaging.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nexcart.messaging.dto.ConversationCreateRequestDTO;
import com.nexcart.messaging.dto.ConversationResponseDTO;
import com.nexcart.messaging.service.ConversationService;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationResponseDTO createConversation(
            @RequestBody ConversationCreateRequestDTO request,
            Authentication authentication) {

        return conversationService.createConversation(
                authentication.getName(),
                request.getProductId());
    }

    @GetMapping
    public Page<ConversationResponseDTO> getConversations(
            Authentication authentication,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        return conversationService.getConversations(
                authentication.getName(),
                pageable);
    }
}
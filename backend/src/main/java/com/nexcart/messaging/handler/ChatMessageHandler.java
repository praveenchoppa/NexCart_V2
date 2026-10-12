
package com.nexcart.messaging.handler;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;

import com.nexcart.messaging.dto.MessageCreateRequestDTO;
import com.nexcart.messaging.dto.MessageResponseDTO;
import com.nexcart.messaging.service.MessageService;

import jakarta.validation.Valid;

@Controller
public class ChatMessageHandler {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessageHandler(
            MessageService messageService,
            SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(
            @Valid @Payload MessageCreateRequestDTO request,
            Principal principal) {

        if (principal == null) {
            throw new AccessDeniedException(
                    "Authentication is required.");
        }

        Long conversationId = request.getConversationId();

        if (conversationId == null) {
            throw new IllegalArgumentException(
                    "Conversation ID is required.");
        }

        MessageResponseDTO savedMessage =
                messageService.sendMessage(
                        principal.getName(),
                        conversationId,
                        request.getContent());

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + conversationId,
                savedMessage);
    }
}

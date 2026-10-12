package com.nexcart.messaging.handler;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

import com.nexcart.messaging.dto.MessageCreateRequestDTO;
import com.nexcart.messaging.dto.MessageResponseDTO;
import com.nexcart.messaging.service.MessageService;

@ExtendWith(MockitoExtension.class)
class ChatMessageHandlerTest {

    @Mock
    private MessageService messageService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ChatMessageHandler chatMessageHandler;

    @Test
    void sendMessage_participant_broadcastsToConversationTopic() {
        MessageCreateRequestDTO request = new MessageCreateRequestDTO();
        request.setConversationId(1L);
        request.setContent("Hello via STOMP");

        MessageResponseDTO saved = new MessageResponseDTO(
                10L,
                1L,
                3L,
                "Rahul",
                "Hello via STOMP",
                LocalDateTime.now(),
                null);

        when(messageService.sendMessage(
                "rahul@nexcart.com",
                1L,
                "Hello via STOMP"))
                .thenReturn(saved);

        chatMessageHandler.sendMessage(request, principal("rahul@nexcart.com"));

        verify(messageService).sendMessage(
                "rahul@nexcart.com",
                1L,
                "Hello via STOMP");
        verify(messagingTemplate).convertAndSend(
                "/topic/conversations/1",
                saved);
        verifyNoMoreInteractions(messagingTemplate);
    }

    @Test
    void sendMessage_missingPrincipal_isRejected() {
        MessageCreateRequestDTO request = new MessageCreateRequestDTO();
        request.setConversationId(1L);
        request.setContent("Hello");

        assertThatThrownBy(() ->
                chatMessageHandler.sendMessage(request, null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Authentication is required.");
    }

    @Test
    void sendMessage_missingConversationId_isRejected() {
        MessageCreateRequestDTO request = new MessageCreateRequestDTO();
        request.setContent("Hello");

        assertThatThrownBy(() ->
                chatMessageHandler.sendMessage(request, principal("rahul@nexcart.com")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Conversation ID is required.");
    }

    @Test
    void sendMessage_usesPrincipalIdentityNotClientSenderId() {
        MessageCreateRequestDTO request = new MessageCreateRequestDTO();
        request.setConversationId(1L);
        request.setContent("Hello");

        MessageResponseDTO saved = new MessageResponseDTO(
                10L,
                1L,
                3L,
                "Rahul",
                "Hello",
                LocalDateTime.now(),
                null);

        when(messageService.sendMessage(
                eq("rahul@nexcart.com"),
                eq(1L),
                eq("Hello")))
                .thenReturn(saved);

        chatMessageHandler.sendMessage(request, principal("rahul@nexcart.com"));

        verify(messageService).sendMessage(
                "rahul@nexcart.com",
                1L,
                "Hello");
    }

    private UsernamePasswordAuthenticationToken principal(String email) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(email)
                .password("unused")
                .roles("USER")
                .build();

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());
    }
}


package com.nexcart.security;

import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

import com.nexcart.messaging.entity.Conversation;
import com.nexcart.messaging.repository.ConversationRepository;
import com.nexcart.user.entity.User;
import com.nexcart.user.repository.UserRepository;

@Component
public class StompAuthorizationInterceptor implements ChannelInterceptor {

    private static final Pattern CONVERSATION_DESTINATION =
            Pattern.compile("^/topic/conversations/(\\d+)$");

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public StompAuthorizationInterceptor(
            ConversationRepository conversationRepository,
            UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();

        if (command == StompCommand.SEND
                || command == StompCommand.SUBSCRIBE) {

            Principal principal = accessor.getUser();

            if (principal == null) {
                throw new AuthenticationCredentialsNotFoundException(
                        "Authentication is required.");
            }

            String destination = accessor.getDestination();

            if (destination == null) {
                throw new AccessDeniedException(
                        "A destination is required.");
            }

            if (command == StompCommand.SUBSCRIBE) {
                authorizeConversationSubscription(
                        principal, destination);
            }

            if (command == StompCommand.SEND) {
                authorizeApplicationDestination(destination);
            }
        }

        return message;
    }

    private void authorizeConversationSubscription(
            Principal principal, String destination) {

        Matcher matcher =
                CONVERSATION_DESTINATION.matcher(destination);

        if (!matcher.matches()) {
            throw new AccessDeniedException(
                    "Subscription to this destination is not allowed.");
        }

        Long conversationId = Long.valueOf(matcher.group(1));

        authorizeParticipant(principal, conversationId);
    }

    private void authorizeParticipant(
            Principal principal, Long conversationId) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() ->
                        new AuthenticationCredentialsNotFoundException(
                                "Authenticated user no longer exists."));

        Conversation conversation =
                conversationRepository.findById(conversationId)
                        .orElseThrow(() ->
                                new AccessDeniedException(
                                        "Conversation access is not allowed."));

        boolean isBuyer =
                conversation.getBuyer().getId().equals(user.getId());

        boolean isSeller =
                conversation.getSeller().getId().equals(user.getId());

        if (!isBuyer && !isSeller) {
            throw new AccessDeniedException(
                    "You are not a participant in this conversation.");
        }
    }

    private void authorizeApplicationDestination(String destination) {

        if (!"/app/chat.send".equals(destination)) {
            throw new AccessDeniedException(
                    "Sending to this destination is not allowed.");
        }
    }
}

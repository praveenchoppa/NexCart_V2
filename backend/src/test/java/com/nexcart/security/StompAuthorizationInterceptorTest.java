package com.nexcart.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

import com.nexcart.messaging.entity.Conversation;
import com.nexcart.messaging.repository.ConversationRepository;
import com.nexcart.user.entity.UserRole;
import com.nexcart.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class StompAuthorizationInterceptorTest {

    private static final String CONVERSATION_TOPIC = "/topic/conversations/1";

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageChannel messageChannel;

    @InjectMocks
    private StompAuthorizationInterceptor interceptor;

    private com.nexcart.user.entity.User buyer;
    private com.nexcart.user.entity.User seller;
    private com.nexcart.user.entity.User arjun;
    private Conversation conversation;

    @BeforeEach
    void setUp() {
        buyer = nexcartUser(3L, "rahul@nexcart.com", "Rahul");
        seller = nexcartUser(1L, "praveen@nexcart.com", "Praveen");
        arjun = nexcartUser(4L, "arjun@nexcart.com", "Arjun");

        conversation = new Conversation();
        conversation.setId(1L);
        conversation.setBuyer(buyer);
        conversation.setSeller(seller);
    }

    @Test
    void subscribe_nonParticipant_throwsAccessDeniedException() {
        when(userRepository.findByEmail("arjun@nexcart.com"))
                .thenReturn(Optional.of(arjun));
        when(conversationRepository.findById(1L))
                .thenReturn(Optional.of(conversation));

        Message<?> message = subscribeMessage("arjun@nexcart.com");

        assertThatThrownBy(() ->
                interceptor.preSend(message, messageChannel))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You are not a participant in this conversation.");
    }

    @Test
    void subscribe_buyerParticipant_allowsSubscription() {
        when(userRepository.findByEmail("rahul@nexcart.com"))
                .thenReturn(Optional.of(buyer));
        when(conversationRepository.findById(1L))
                .thenReturn(Optional.of(conversation));

        Message<?> message = subscribeMessage("rahul@nexcart.com");

        Message<?> result = interceptor.preSend(message, messageChannel);

        assertThat(result).isSameAs(message);
    }

    @Test
    void subscribe_missingPrincipal_isRejected() {
        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setLeaveMutable(true);
        accessor.setDestination(CONVERSATION_TOPIC);

        Message<?> message =
                MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThatThrownBy(() ->
                interceptor.preSend(message, messageChannel))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
                .hasMessage("Authentication is required.");
    }

    @Test
    void subscribe_nonexistentConversation_isDenied() {
        when(userRepository.findByEmail("rahul@nexcart.com"))
                .thenReturn(Optional.of(buyer));
        when(conversationRepository.findById(1L))
                .thenReturn(Optional.empty());

        Message<?> message = subscribeMessage("rahul@nexcart.com");

        assertThatThrownBy(() ->
                interceptor.preSend(message, messageChannel))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Conversation access is not allowed.");
    }

    @Test
    void subscribe_unsupportedDestination_isDenied() {
        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setLeaveMutable(true);
        accessor.setDestination("/topic/other/1");
        accessor.setUser(authenticationFor("rahul@nexcart.com"));

        Message<?> message =
                MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThatThrownBy(() ->
                interceptor.preSend(message, messageChannel))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Subscription to this destination is not allowed.");
    }

    @Test
    void send_allowedApplicationDestination_isPermitted() {
        Message<?> message = sendMessage("rahul@nexcart.com", "/app/chat.send");

        Message<?> result = interceptor.preSend(message, messageChannel);

        assertThat(result).isSameAs(message);
    }

    @Test
    void send_unsupportedApplicationDestination_isDenied() {
        Message<?> message = sendMessage("rahul@nexcart.com", "/app/other.send");

        assertThatThrownBy(() ->
                interceptor.preSend(message, messageChannel))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Sending to this destination is not allowed.");
    }

    @Test
    void subscribe_sellerParticipant_allowsSubscription() {
        when(userRepository.findByEmail("praveen@nexcart.com"))
                .thenReturn(Optional.of(seller));
        when(conversationRepository.findById(1L))
                .thenReturn(Optional.of(conversation));

        Message<?> message = subscribeMessage("praveen@nexcart.com");

        Message<?> result = interceptor.preSend(message, messageChannel);

        assertThat(result).isSameAs(message);
    }

    private Message<?> sendMessage(String email, String destination) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setLeaveMutable(true);
        accessor.setDestination(destination);
        accessor.setUser(authenticationFor(email));

        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private Message<?> subscribeMessage(String email) {
        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setLeaveMutable(true);
        accessor.setDestination(CONVERSATION_TOPIC);
        accessor.setUser(authenticationFor(email));

        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private UsernamePasswordAuthenticationToken authenticationFor(String email) {
        UserDetails springUser = org.springframework.security.core.userdetails.User
                .withUsername(email)
                .password("unused")
                .roles(UserRole.USER.name())
                .build();

        return new UsernamePasswordAuthenticationToken(
                springUser,
                null,
                springUser.getAuthorities());
    }

    private com.nexcart.user.entity.User nexcartUser(
            Long id,
            String email,
            String name) {

        com.nexcart.user.entity.User user = new com.nexcart.user.entity.User();
        user.setId(id);
        user.setEmail(email);
        user.setName(name);
        user.setRole(UserRole.USER);
        return user;
    }
}

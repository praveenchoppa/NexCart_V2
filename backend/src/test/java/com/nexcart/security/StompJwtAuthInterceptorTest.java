package com.nexcart.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class StompJwtAuthInterceptorTest {

    private static final String EMAIL = "rahul@nexcart.com";
    private static final String VALID_TOKEN = "valid.jwt.token";

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private MessageChannel messageChannel;

    @InjectMocks
    private StompJwtAuthInterceptor interceptor;

    @Test
    void connect_validJwt_establishesAuthenticatedPrincipal() {
        UserDetails userDetails = userDetails(EMAIL);

        when(jwtService.isTokenValid(VALID_TOKEN)).thenReturn(true);
        when(jwtService.extractEmail(VALID_TOKEN)).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);

        Message<?> message = connectMessage("Bearer " + VALID_TOKEN);

        Message<?> result = interceptor.preSend(message, messageChannel);

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(result);
        assertThat(accessor.getUser()).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(accessor.getUser().getName()).isEqualTo(EMAIL);
        verify(userDetailsService).loadUserByUsername(EMAIL);
    }

    @Test
    void connect_missingAuthorizationHeader_isRejected() {
        Message<?> message = connectMessage(null);

        assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Missing or invalid Authorization header");
    }

    @Test
    void connect_malformedAuthorizationHeader_isRejected() {
        Message<?> message = connectMessage("Token " + VALID_TOKEN);

        assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Missing or invalid Authorization header");
    }

    @Test
    void connect_invalidJwt_isRejected() {
        when(jwtService.isTokenValid("bad.token")).thenReturn(false);

        Message<?> message = connectMessage("Bearer bad.token");

        assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid or expired JWT");
    }

    @Test
    void connect_expiredJwt_isRejected() {
        when(jwtService.isTokenValid("expired.token")).thenReturn(false);

        Message<?> message = connectMessage("Bearer expired.token");

        assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid or expired JWT");
    }

    @Test
    void connect_userNoLongerExists_isRejected() {
        when(jwtService.isTokenValid(VALID_TOKEN)).thenReturn(true);
        when(jwtService.extractEmail(VALID_TOKEN)).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL))
                .thenThrow(new UsernameNotFoundException("User not found"));

        Message<?> message = connectMessage("Bearer " + VALID_TOKEN);

        assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void subscribeFrame_isNotHandledByJwtInterceptor() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setLeaveMutable(true);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, messageChannel);

        assertThat(result).isSameAs(message);
        assertThat(StompHeaderAccessor.wrap(result).getUser()).isNull();
    }

    private Message<?> connectMessage(String authorizationHeader) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        if (authorizationHeader != null) {
            accessor.setNativeHeader("Authorization", authorizationHeader);
        }

        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private UserDetails userDetails(String email) {
        return org.springframework.security.core.userdetails.User
                .withUsername(email)
                .password("unused")
                .roles("USER")
                .build();
    }
}

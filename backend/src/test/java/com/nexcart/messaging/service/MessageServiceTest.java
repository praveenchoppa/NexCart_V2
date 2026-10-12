package com.nexcart.messaging.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;

import com.nexcart.exception.ResourceNotFoundException;
import com.nexcart.messaging.dto.MessageResponseDTO;
import com.nexcart.messaging.entity.Conversation;
import com.nexcart.messaging.entity.Message;
import com.nexcart.messaging.repository.ConversationRepository;
import com.nexcart.messaging.repository.MessageRepository;
import com.nexcart.user.entity.User;
import com.nexcart.user.entity.UserRole;
import com.nexcart.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MessageService messageService;

    private User buyer;
    private User seller;
    private User outsider;
    private Conversation conversation;

    @BeforeEach
    void setUp() {
        buyer = user(3L, "rahul@nexcart.com", "Rahul");
        seller = user(1L, "praveen@nexcart.com", "Praveen");
        outsider = user(4L, "arjun@nexcart.com", "Arjun");

        conversation = new Conversation();
        conversation.setId(1L);
        conversation.setBuyer(buyer);
        conversation.setSeller(seller);
    }

    @Test
    void sendMessage_participant_persistsAndReturnsExpectedFields() {
        when(userRepository.findByEmail("rahul@nexcart.com"))
                .thenReturn(Optional.of(buyer));
        when(conversationRepository.findById(1L))
                .thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message saved = invocation.getArgument(0);
            saved.setId(10L);
            saved.setCreatedAt(LocalDateTime.now());
            return saved;
        });

        MessageResponseDTO response =
                messageService.sendMessage("rahul@nexcart.com", 1L, "Hello");

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());

        Message saved = captor.getValue();
        assertThat(saved.getSender()).isEqualTo(buyer);
        assertThat(saved.getConversation()).isEqualTo(conversation);
        assertThat(saved.getContent()).isEqualTo("Hello");

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getConversationId()).isEqualTo(1L);
        assertThat(response.getSenderId()).isEqualTo(3L);
        assertThat(response.getSenderName()).isEqualTo("Rahul");
        assertThat(response.getContent()).isEqualTo("Hello");
    }

    @Test
    void sendMessage_nonParticipant_isRejected() {
        when(userRepository.findByEmail("arjun@nexcart.com"))
                .thenReturn(Optional.of(outsider));
        when(conversationRepository.findById(1L))
                .thenReturn(Optional.of(conversation));

        assertThatThrownBy(() ->
                messageService.sendMessage("arjun@nexcart.com", 1L, "Hello"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You are not a participant in this conversation!");
    }

    @Test
    void sendMessage_missingConversation_throwsNotFound() {
        when(userRepository.findByEmail("rahul@nexcart.com"))
                .thenReturn(Optional.of(buyer));
        when(conversationRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                messageService.sendMessage("rahul@nexcart.com", 99L, "Hello"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Conversation not found!");
    }

    @Test
    void getMessages_participant_returnsPagedHistory() {
        Message stored = new Message();
        stored.setId(10L);
        stored.setConversation(conversation);
        stored.setSender(buyer);
        stored.setContent("Earlier message");
        stored.setCreatedAt(LocalDateTime.now());

        PageRequest pageable = PageRequest.of(0, 20);
        Page<Message> page = new PageImpl<>(List.of(stored));

        when(userRepository.findByEmail("rahul@nexcart.com"))
                .thenReturn(Optional.of(buyer));
        when(conversationRepository.findById(1L))
                .thenReturn(Optional.of(conversation));
        when(messageRepository.findByConversationIdOrderByCreatedAtAsc(1L, pageable))
                .thenReturn(page);

        Page<MessageResponseDTO> result =
                messageService.getMessages("rahul@nexcart.com", 1L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("Earlier message");
        verify(messageRepository).findByConversationIdOrderByCreatedAtAsc(eq(1L), eq(pageable));
    }

    @Test
    void getMessages_nonParticipant_isRejected() {
        when(userRepository.findByEmail("arjun@nexcart.com"))
                .thenReturn(Optional.of(outsider));
        when(conversationRepository.findById(1L))
                .thenReturn(Optional.of(conversation));

        assertThatThrownBy(() ->
                messageService.getMessages("arjun@nexcart.com", 1L, PageRequest.of(0, 20)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You are not a participant in this conversation!");
    }

    private User user(Long id, String email, String name) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setName(name);
        user.setRole(UserRole.USER);
        return user;
    }
}

package com.nexcart.messaging.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.nexcart.exception.ResourceNotFoundException;
import com.nexcart.messaging.dto.MessageResponseDTO;
import com.nexcart.messaging.entity.Conversation;
import com.nexcart.messaging.entity.Message;
import com.nexcart.messaging.repository.ConversationRepository;
import com.nexcart.messaging.repository.MessageRepository;
import com.nexcart.user.entity.User;
import com.nexcart.user.repository.UserRepository;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public MessageService(
            MessageRepository messageRepository,
            ConversationRepository conversationRepository,
            UserRepository userRepository) {

        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    public MessageResponseDTO sendMessage(
            String email,
            Long conversationId,
            String content) {

        User sender = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found!"));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Conversation not found!"));

        if (!conversation.getBuyer().getId().equals(sender.getId())
                && !conversation.getSeller().getId().equals(sender.getId())) {

            throw new AccessDeniedException(
                    "You are not a participant in this conversation!");
        }

        Message message = new Message();

        message.setContent(content);
        message.setSender(sender);
        message.setConversation(conversation);

        Message savedMessage = messageRepository.save(message);

        return toResponseDTO(savedMessage);
    }

    private MessageResponseDTO toResponseDTO(Message message) {

        return new MessageResponseDTO(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getSender().getName(),
                message.getContent(),
                message.getCreatedAt(),
                message.getReadAt()
        );
    }

    public Page<MessageResponseDTO> getMessages(
            String email,
            Long conversationId,
            Pageable pageable) {

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found!"));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Conversation not found!"));
        
        if (!conversation.getBuyer().getId().equals(currentUser.getId())
                && !conversation.getSeller().getId().equals(currentUser.getId())) {

            throw new AccessDeniedException(
                    "You are not a participant in this conversation!");
        }

        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, pageable)
                .map(this::toResponseDTO);
    }
}
package com.nexcart.messaging.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.nexcart.exception.BadRequestException;
import com.nexcart.exception.ProductNotFoundException;
import com.nexcart.exception.ResourceNotFoundException;
import com.nexcart.messaging.dto.ConversationResponseDTO;
import com.nexcart.messaging.entity.Conversation;
import com.nexcart.messaging.repository.ConversationRepository;
import com.nexcart.product.entity.Product;
import com.nexcart.product.repository.ProductRepository;
import com.nexcart.user.entity.User;
import com.nexcart.user.repository.UserRepository;


@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ConversationService(
            ConversationRepository conversationRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {

        this.conversationRepository = conversationRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public ConversationResponseDTO createConversation(String email, Long productId){
        
        User buyer = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found!"));

        User seller = product.getSeller();

        if(seller == null){
            throw new BadRequestException("Product does not have a seller!");
        }

        if(buyer.getId().equals(seller.getId())){
            throw new BadRequestException("Buyer and seller cannot be the same user!");
        }

        return toResponseDTO(conversationRepository.findByBuyerIdAndSellerIdAndProductId(buyer.getId(), seller.getId(), product.getId())
            .orElseGet(() -> {
                Conversation conversation = new Conversation();
                conversation.setBuyer(buyer);
                conversation.setSeller(seller);
                conversation.setProduct(product);
                return conversationRepository.save(conversation);
            })
        );
    }

    private ConversationResponseDTO toResponseDTO(Conversation conversation){

        return new ConversationResponseDTO(
            conversation.getId(),
            
            conversation.getBuyer().getId(),
            conversation.getBuyer().getName(),

            conversation.getSeller().getId(),
            conversation.getSeller().getName(),

            conversation.getProduct().getId(),
            conversation.getProduct().getName(),

            conversation.getCreatedAt(),
            conversation.getUpdatedAt()
        );
    }

    public Page<ConversationResponseDTO> getConversations(String email, Pageable pageable){

        User currentUser = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

            return conversationRepository.findByBuyerIdOrSellerId(currentUser.getId(), currentUser.getId(), pageable)
                .map(this::toResponseDTO);
    } 
}
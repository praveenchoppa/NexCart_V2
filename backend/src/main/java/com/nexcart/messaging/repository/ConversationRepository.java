package com.nexcart.messaging.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexcart.messaging.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByBuyerIdAndSellerIdAndProductId(
            Long buyerId,
            Long sellerId,
            Long productId
    );

    Page<Conversation> findByBuyerIdOrSellerId(
            Long buyerId,
            Long sellerId,
            Pageable pageable
    );
}
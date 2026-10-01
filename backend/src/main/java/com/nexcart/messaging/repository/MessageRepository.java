package com.nexcart.messaging.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexcart.messaging.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
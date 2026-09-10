package com.nexcart.order.service;

import com.nexcart.cart.entity.Cart;
import com.nexcart.cart.entity.CartItem;
import com.nexcart.cart.repository.CartRepository;
import com.nexcart.exception.BadRequestException;
import com.nexcart.exception.ResourceNotFoundException;
import com.nexcart.order.dto.OrderItemResponseDTO;
import com.nexcart.order.dto.OrderResponseDTO;
import com.nexcart.order.entity.Order;
import com.nexcart.order.entity.OrderItem;
import com.nexcart.order.entity.OrderStatus;
import com.nexcart.order.repository.OrderRepository;
import com.nexcart.product.entity.Product;
import com.nexcart.user.entity.User;
import com.nexcart.user.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    public OrderService(
            OrderRepository orderRepository,
            CartRepository cartRepository,
            UserRepository userRepository) {

        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderResponseDTO createOrder(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found!"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart not found!"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty!");
        }

        Order order = new Order();

        order.setUser(user);
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {

                Product product = cartItem.getProduct();

                if(cartItem.getQuantity() > product.getStock()){
                        throw new BadRequestException("Insufficient stock!");
                }

            product.setStock(product.getStock() - cartItem.getQuantity());
        
            BigDecimal price = product.getPrice();

            BigDecimal subtotal = price.multiply(
                    BigDecimal.valueOf(cartItem.getQuantity())
            );

            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setPrice(price);
            orderItem.setQuantity(cartItem.getQuantity());

            order.getItems().add(orderItem);

            totalAmount = totalAmount.add(subtotal);
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        cart.getItems().clear();

        List<OrderItemResponseDTO> responseItems = new ArrayList<>();

        for (OrderItem orderItem : savedOrder.getItems()) {

            BigDecimal subtotal = orderItem.getPrice().multiply(
                    BigDecimal.valueOf(orderItem.getQuantity())
            );

            responseItems.add(
                    new OrderItemResponseDTO(
                            orderItem.getId(),
                            orderItem.getProduct().getId(),
                            orderItem.getProduct().getName(),
                            orderItem.getPrice(),
                            orderItem.getQuantity(),
                            subtotal
                    )
            );
        }

        OrderResponseDTO response = new OrderResponseDTO(
                savedOrder.getId(),
                savedOrder.getStatus().name(),
                savedOrder.getTotalAmount(),
                savedOrder.getCreatedAt(),
                responseItems
        );

        return response;
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(String email, Long orderId){

        Order order = orderRepository.findByIdAndUserEmail(orderId,email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found!"));

        List<OrderItemResponseDTO> responseItems = new ArrayList<>();

        for (OrderItem orderItem : order.getItems()) {

            BigDecimal subtotal = orderItem.getPrice().multiply(
                    BigDecimal.valueOf(orderItem.getQuantity())
            );

            responseItems.add(
                    new OrderItemResponseDTO(
                            orderItem.getId(),
                            orderItem.getProduct().getId(),
                            orderItem.getProduct().getName(),
                            orderItem.getPrice(),
                            orderItem.getQuantity(),
                            subtotal
                    )
            );
        }

        return new OrderResponseDTO(
                order.getId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                responseItems
        );
    }

        @Transactional(readOnly = true)
        public Page<OrderResponseDTO> getUserOrders(
                String email, Pageable pageable){

                Page<Order> ordersPage = orderRepository.findByUserEmail(email, pageable);
                
                return ordersPage.map(order -> {
                    List<OrderItemResponseDTO> responseItems = new ArrayList<>();

                    for (OrderItem orderItem : order.getItems()) {

                        BigDecimal subtotal = orderItem.getPrice().multiply(
                                BigDecimal.valueOf(orderItem.getQuantity())
                        );

                        responseItems.add(
                                new OrderItemResponseDTO(
                                        orderItem.getId(),
                                        orderItem.getProduct().getId(),
                                        orderItem.getProduct().getName(),
                                        orderItem.getPrice(),
                                        orderItem.getQuantity(),
                                        subtotal
                                )
                        );
                    }

                    return new OrderResponseDTO(
                            order.getId(),
                            order.getStatus().name(),
                            order.getTotalAmount(),
                            order.getCreatedAt(),
                            responseItems
                    );
                });
        }
}
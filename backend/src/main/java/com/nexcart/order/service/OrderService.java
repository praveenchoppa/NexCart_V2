package com.nexcart.order.service;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.nexcart.product.repository.ProductRepository;
import com.nexcart.user.entity.User;
import com.nexcart.user.repository.UserRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(
            OrderRepository orderRepository,
            CartRepository cartRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {

        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderResponseDTO createOrder(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found!"
                        ));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart not found!"
                        ));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException(
                    "Cart is empty!"
            );
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PLACED);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {

            Product product = productRepository
                    .findLockedById(
                            cartItem.getProduct().getId()
                    )
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Product not found!"
                            ));

            if (product.getStock() < cartItem.getQuantity()) {
                throw new BadRequestException(
                        "Insufficient stock for product: "
                                + product.getName()
                );
            }
        }

        for (CartItem cartItem : cart.getItems()) {

            Product product = productRepository
                    .findLockedById(
                            cartItem.getProduct().getId()
                    )
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Product not found!"
                            ));

            product.setStock(
                    product.getStock()
                            - cartItem.getQuantity()
            );

            BigDecimal itemSubtotal =
                    product.getPrice().multiply(
                            BigDecimal.valueOf(
                                    cartItem.getQuantity()
                            )
                    );

            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);
            orderItem.setProduct(product);


            orderItem.setPrice(product.getPrice());

            orderItem.setQuantity(
                    cartItem.getQuantity()
            );

            order.getItems().add(orderItem);

            totalAmount = totalAmount.add(itemSubtotal);
        }

        order.setTotalAmount(totalAmount);

        orderRepository.save(order);

        cart.getItems().clear();

        return new OrderResponseDTO(
                order.getId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getItems()
                        .stream()
                        .map(orderItem -> {

                            BigDecimal subtotal =
                                    orderItem.getPrice().multiply(
                                            BigDecimal.valueOf(
                                                    orderItem.getQuantity()
                                            )
                                    );

                            return new OrderItemResponseDTO(
                                    orderItem.getId(),
                                    orderItem.getProduct().getId(),
                                    orderItem.getProduct().getName(),
                                    orderItem.getPrice(),
                                    orderItem.getQuantity(),
                                    subtotal
                            );
                        })
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(
            String email,
            Long orderId) {

        Order order = orderRepository
                .findByIdAndUserEmail(orderId, email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found!"
                        ));

        return new OrderResponseDTO(
                order.getId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getItems()
                        .stream()
                        .map(orderItem -> {

                            BigDecimal subtotal =
                                    orderItem.getPrice().multiply(
                                            BigDecimal.valueOf(
                                                    orderItem.getQuantity()
                                            )
                                    );

                            return new OrderItemResponseDTO(
                                    orderItem.getId(),
                                    orderItem.getProduct().getId(),
                                    orderItem.getProduct().getName(),
                                    orderItem.getPrice(),
                                    orderItem.getQuantity(),
                                    subtotal
                            );
                        })
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getUserOrders(
            String email,
            Pageable pageable) {

        return orderRepository
                .findByUserEmail(email, pageable)
                .map(order -> {

                    return new OrderResponseDTO(
                            order.getId(),
                            order.getStatus().name(),
                            order.getTotalAmount(),
                            order.getCreatedAt(),
                            order.getItems()
                                    .stream()
                                    .map(orderItem -> {

                                        BigDecimal subtotal =
                                                orderItem.getPrice().multiply(
                                                        BigDecimal.valueOf(
                                                                orderItem.getQuantity()
                                                        )
                                                );

                                        return new OrderItemResponseDTO(
                                                orderItem.getId(),
                                                orderItem.getProduct().getId(),
                                                orderItem.getProduct().getName(),
                                                orderItem.getPrice(),
                                                orderItem.getQuantity(),
                                                subtotal
                                        );
                                    })
                                    .toList()
                    );
                });
    }

    @Transactional (readOnly = true)
    public Page<OrderResponseDTO> getAllOrders(Pageable pageable){

        return orderRepository.findAll(pageable).map(order -> {
                return new OrderResponseDTO(
                        order.getId(),
                        order.getStatus().name(),
                        order.getTotalAmount(),
                        order.getCreatedAt(),
                        order.getItems()
                                .stream()
                                .map(orderItem -> {

                                    BigDecimal subtotal =
                                            orderItem.getPrice().multiply(
                                                    BigDecimal.valueOf(
                                                            orderItem.getQuantity()
                                                    )
                                            );

                                    return new OrderItemResponseDTO(
                                            orderItem.getId(),
                                            orderItem.getProduct().getId(),
                                            orderItem.getProduct().getName(),
                                            orderItem.getPrice(),
                                            orderItem.getQuantity(),
                                            subtotal
                                    );
                                })
                                .toList()
                );
        });
    }

    @Transactional
    public OrderResponseDTO updateOrderStatus(
            Long orderId,
            OrderStatus newStatus) {

        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found!"
                        ));

        OrderStatus currentStatus =
                order.getStatus();

        if (!isValidTransition(
                currentStatus,
                newStatus)) {

            throw new BadRequestException(
                    "Invalid order status transition!"
            );
        }

        if(newStatus == OrderStatus.CANCELLED){
                for(OrderItem orderItem : order.getItems()){
                        Product product = productRepository.findLockedById(orderItem.getProduct().getId())
                                .orElseThrow(() ->
                                        new ResourceNotFoundException(
                                                "Product not found!"
                                        ));

                        product.setStock(
                                product.getStock() + orderItem.getQuantity()
                        );
                }
        }
        order.setStatus(newStatus);

        return new OrderResponseDTO(
                order.getId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getItems()
                        .stream()
                        .map(orderItem -> {

                            BigDecimal subtotal =
                                    orderItem.getPrice().multiply(
                                            BigDecimal.valueOf(
                                                    orderItem.getQuantity()
                                            )
                                    );

                            return new OrderItemResponseDTO(
                                    orderItem.getId(),
                                    orderItem.getProduct().getId(),
                                    orderItem.getProduct().getName(),
                                    orderItem.getPrice(),
                                    orderItem.getQuantity(),
                                    subtotal
                            );
                        })
                        .toList()
        );
    }

    private boolean isValidTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus) {

        return switch (currentStatus) {

            case PLACED ->
                    newStatus == OrderStatus.CONFIRMED
                    || newStatus == OrderStatus.CANCELLED;

            case CONFIRMED ->
                    newStatus == OrderStatus.SHIPPED
                    || newStatus == OrderStatus.CANCELLED;

            case SHIPPED ->
                    newStatus == OrderStatus.DELIVERED;

            case DELIVERED, CANCELLED ->
                    false;
        };
    }
}
package com.nexcart.order.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import com.nexcart.order.repository.OrderItemRepository;
import com.nexcart.order.repository.OrderRepository;
import com.nexcart.product.entity.Product;
import com.nexcart.product.repository.ProductRepository;
import com.nexcart.user.entity.User;
import com.nexcart.user.repository.UserRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CartRepository cartRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
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
                        .map(this::toOrderItemResponseDTO)
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
                        .map(this::toOrderItemResponseDTO)
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
                                    .map(this::toOrderItemResponseDTO)
                                    .toList()
                    );
                });
    }

@Transactional(readOnly = true)
public Page<OrderResponseDTO> getOrdersForSeller(
        String email,
        Pageable pageable) {

    User seller = userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "User not found!"
                    ));

    Page<OrderItem> orderItems =
            orderItemRepository.findByProductSellerId(
                    seller.getId(),
                    pageable
            );

    Map<Long, OrderResponseDTO> orderMap =
            new LinkedHashMap<>();

    for (OrderItem orderItem : orderItems.getContent()) {

        Order order = orderItem.getOrder();

        OrderResponseDTO orderResponse =
                orderMap.computeIfAbsent(
                        order.getId(),
                        id -> new OrderResponseDTO(
                                order.getId(),
                                order.getStatus().name(),
                                BigDecimal.ZERO,
                                order.getCreatedAt(),
                                new ArrayList<>()
                        )
                );

        orderResponse.getItems()
                .add(toOrderItemResponseDTO(orderItem));

        BigDecimal itemSubtotal =
                orderItem.getPrice()
                        .multiply(
                                BigDecimal.valueOf(
                                        orderItem.getQuantity()
                                )
                        );

        orderResponse.setTotalAmount(
                orderResponse.getTotalAmount()
                        .add(itemSubtotal)
        );
    }

    return new PageImpl<>(
            new ArrayList<>(orderMap.values()),
            pageable,
            orderMap.size()
    );
}

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getAllOrders(
            Pageable pageable) {

        return orderRepository
                .findAll(pageable)
                .map(order -> {

                    return new OrderResponseDTO(
                            order.getId(),
                            order.getStatus().name(),
                            order.getTotalAmount(),
                            order.getCreatedAt(),
                            order.getItems()
                                    .stream()
                                    .map(this::toOrderItemResponseDTO)
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

        if (newStatus == OrderStatus.CANCELLED) {

            for (OrderItem orderItem : order.getItems()) {

                Product product = productRepository
                        .findLockedById(
                                orderItem.getProduct().getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Product not found!"
                                ));

                product.setStock(
                        product.getStock()
                                + orderItem.getQuantity()
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
                        .map(this::toOrderItemResponseDTO)
                        .toList()
        );
    }

    private OrderItemResponseDTO toOrderItemResponseDTO(
            OrderItem orderItem) {

        BigDecimal subtotal =
                orderItem.getPrice()
                        .multiply(
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
                subtotal,
                orderItem.getProduct().getSeller().getId(),
                orderItem.getProduct().getSeller().getName()
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
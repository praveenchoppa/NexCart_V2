package com.nexcart.cart.service;

import com.nexcart.cart.dto.AddToCartRequestDTO;
import com.nexcart.cart.dto.CartItemResponseDTO;
import com.nexcart.cart.dto.CartResponseDTO;
import com.nexcart.cart.dto.UpdateCartItemRequestDTO;
import com.nexcart.cart.entity.Cart;
import com.nexcart.cart.entity.CartItem;
import com.nexcart.cart.repository.CartItemRepository;
import com.nexcart.cart.repository.CartRepository;
import com.nexcart.exception.ResourceNotFoundException;
import com.nexcart.product.entity.Product;
import com.nexcart.product.repository.ProductRepository;
import com.nexcart.user.entity.User;
import com.nexcart.user.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public void addToCart(String email, AddToCartRequestDTO request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found!"));

        Long userId = user.getId();

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found!"));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        if (cartItem != null) {

            cartItem.setQuantity(
                    cartItem.getQuantity() + request.getQuantity()
            );

        } else {

            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
        }

        cartItemRepository.save(cartItem);
    }

    public CartResponseDTO getCart(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found!"));

        Long userId = user.getId();

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart not found!"));

        List<CartItemResponseDTO> items = cart.getItems()
                .stream()
                .map(item -> {

                    BigDecimal subtotal = item.getProduct()
                            .getPrice()
                            .multiply(
                                    BigDecimal.valueOf(item.getQuantity())
                            );

                    return new CartItemResponseDTO(
                            item.getId(),
                            item.getProduct().getId(),
                            item.getProduct().getName(),
                            item.getProduct().getPrice(),
                            item.getQuantity(),
                            subtotal
                    );
                })
                .toList();

        BigDecimal totalPrice = items.stream()
                .map(CartItemResponseDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponseDTO(
                cart.getId(),
                items,
                totalPrice
        );
    }

    public void updateCartItem(
            String email,
            Long itemId,
            UpdateCartItemRequestDTO request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found!"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart not found!"));

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart item not found!"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {

            throw new ResourceNotFoundException(
                    "Cart item not found!"
            );
        }

        cartItem.setQuantity(request.getQuantity());

        cartItemRepository.save(cartItem);
    }

    public void removeFromCart(String email, Long itemId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found!"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart not found!"));

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart item not found!"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {

            throw new ResourceNotFoundException(
                    "Cart item not found!"
            );
        }

        cartItemRepository.delete(cartItem);
    }
}
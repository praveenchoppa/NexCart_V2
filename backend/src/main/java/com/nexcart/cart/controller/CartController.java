package com.nexcart.cart.controller;

import com.nexcart.cart.dto.AddToCartRequestDTO;
import com.nexcart.cart.dto.CartResponseDTO;
import com.nexcart.cart.dto.UpdateCartItemRequestDTO;
import com.nexcart.cart.service.CartService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/items")
    public ResponseEntity<Void> addToCart(
            @Valid @RequestBody AddToCartRequestDTO request,
            Authentication authentication) {

        String email = authentication.getName();

        cartService.addToCart(email, request);

        return ResponseEntity.ok().build();
    }

    @GetMapping
    public CartResponseDTO getCart(Authentication authentication) {

        String email = authentication.getName();

        return cartService.getCart(email);
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<Void> updateCartItem(@PathVariable Long itemId,@Valid @RequestBody UpdateCartItemRequestDTO request,Authentication authentication){

        String email = authentication.getName();

        cartService.updateCartItem(email, itemId, request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long itemId){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        cartService.removeFromCart(email,itemId);

        return ResponseEntity.noContent().build();
    }
}
package com.nexcart.user.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexcart.user.dto.UserRegistrationRequestDTO;
import com.nexcart.user.dto.UserRegistrationResponseDTO;
import com.nexcart.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;

    @PostMapping("/register")
    public UserRegistrationResponseDTO registerUser(@Valid @RequestBody UserRegistrationRequestDTO request) {
        return userService.registerUser(request);
    }
}

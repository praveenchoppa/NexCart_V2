package com.nexcart.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nexcart.exception.UserAlreadyExistsException;
import com.nexcart.user.dto.UserRegistrationRequestDTO;
import com.nexcart.user.dto.UserRegistrationResponseDTO;
import com.nexcart.user.entity.User;
import com.nexcart.user.entity.UserRole;
import com.nexcart.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationResponseDTO registerUser(
            UserRegistrationRequestDTO request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "User with this email already exists!");
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());


        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setRole(UserRole.USER);

        User savedUser = userRepository.save(user);

        UserRegistrationResponseDTO response =
                new UserRegistrationResponseDTO();

        response.setId(savedUser.getId());
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());
        response.setRole(savedUser.getRole());

        return response;
    }
}
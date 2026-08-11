package com.nexcart.user.dto;

import com.nexcart.user.entity.UserRole;

import lombok.*;

@Getter
@Setter
public class UserRegistrationResponseDTO {
    
    private Long id;
    private String name;
    private String email;
    private UserRole role;
}

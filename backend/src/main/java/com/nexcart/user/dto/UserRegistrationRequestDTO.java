package com.nexcart.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
public class UserRegistrationRequestDTO {
    
    @NotBlank(message = "Name is required!")
    @Size(max = 100,message = "Name must not exceed 100 characters!")
    private String name;

    @NotBlank(message = "Email is required!")
    @Email(message = "Invalid email format!")
    private String email;

    @NotBlank(message = "Password is required!")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters!")
    private String password;
}

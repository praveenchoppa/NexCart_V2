package com.nexcart.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CategoryRequestDTO {
    
    @NotBlank(message = "Category name is required!")
    @Size(max = 100, message = "Category name cannot exceed 100 characters!")
    private String name;

    @Size(max = 500, message = "Category description cannot exceed 500 characters!")
    private String description;
}

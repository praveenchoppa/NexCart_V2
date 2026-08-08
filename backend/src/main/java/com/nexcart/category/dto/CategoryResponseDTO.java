package com.nexcart.category.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class CategoryResponseDTO {
    
    private Long id;

    private String name;
    
    private String description;
}

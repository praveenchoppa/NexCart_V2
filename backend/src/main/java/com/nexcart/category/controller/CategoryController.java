package com.nexcart.category.controller;

import org.springframework.web.bind.annotation.*;

import com.nexcart.category.dto.CategoryRequestDTO;
import com.nexcart.category.dto.CategoryResponseDTO;
import com.nexcart.category.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;


    @PostMapping
    public CategoryResponseDTO createCategory(@Valid @RequestBody CategoryRequestDTO request){                              
        return categoryService.createCategory(request);        
    }
}
  
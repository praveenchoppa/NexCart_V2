package com.nexcart.category.service;

import org.springframework.stereotype.Service;

import com.nexcart.category.dto.CategoryRequestDTO;
import com.nexcart.category.dto.CategoryResponseDTO;
import com.nexcart.category.entity.Category;
import com.nexcart.category.repository.CategoryRepository;
import com.nexcart.exception.CategoryAlreadyExistsException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {


    private final CategoryRepository categoryRepository;

    public CategoryResponseDTO createCategory(CategoryRequestDTO request) {
        
        if(categoryRepository.existsByName(request.getName())){
            throw new CategoryAlreadyExistsException("Category already exists");
        }

        Category category =  new Category(
            request.getName(),
            request.getDescription()
        );

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category savedCategory = categoryRepository.save(category);

        CategoryResponseDTO response = new CategoryResponseDTO();

        response.setId(savedCategory.getId());
        response.setName(savedCategory.getName());
        response.setDescription(savedCategory.getDescription());
        return response;
    }
    
}

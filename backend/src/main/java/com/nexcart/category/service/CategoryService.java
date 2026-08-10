package com.nexcart.category.service;

import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nexcart.category.dto.CategoryRequestDTO;
import com.nexcart.category.dto.CategoryResponseDTO;
import com.nexcart.category.entity.Category;
import com.nexcart.category.repository.CategoryRepository;
import com.nexcart.exception.CategoryAlreadyExistsException;
import com.nexcart.exception.CategoryNotFoundException;

import com.nexcart.exception.CategoryInUseException;
import com.nexcart.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {


    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

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

    public CategoryResponseDTO getCategoryById(Long id){

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                    new CategoryNotFoundException("Category not found!"));

        CategoryResponseDTO response = new CategoryResponseDTO();

        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());

        return response;
    }

    public Page<CategoryResponseDTO> getAllCategories(Pageable pageable){

        Page<Category> categories = categoryRepository.findAll(pageable);

        return categories.map(category -> {

            CategoryResponseDTO response = new CategoryResponseDTO();
            response.setId(category.getId());
            response.setName(category.getName());
            response.setDescription(category.getDescription());
            return response;
        });
    }
    
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found!"));

        if(categoryRepository.existsByName(request.getName())
                && !category.getName().equals(request.getName())){
            throw new CategoryAlreadyExistsException("Category already exists");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category updatedCategory = categoryRepository.save(category);

        CategoryResponseDTO response = new CategoryResponseDTO();

        response.setId(updatedCategory.getId());
        response.setName(updatedCategory.getName());
        response.setDescription(updatedCategory.getDescription());

        return response;
    }

    public void deleteCategory(Long id){

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found!"));

        if(productRepository.existsByCategoryId(id)){
            throw new CategoryInUseException("Cannot delete category. Products are associated with it.");
        }

        categoryRepository.delete(category);
    }
}

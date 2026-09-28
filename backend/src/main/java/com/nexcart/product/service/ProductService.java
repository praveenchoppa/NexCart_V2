package com.nexcart.product.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.nexcart.category.entity.Category;
import com.nexcart.category.repository.CategoryRepository;
import com.nexcart.exception.CategoryNotFoundException;
import com.nexcart.exception.ProductNotFoundException;
import com.nexcart.exception.ResourceNotFoundException;
import com.nexcart.product.dto.ProductRequestDTO;
import com.nexcart.product.dto.ProductResponseDTO;
import com.nexcart.product.entity.Product;
import com.nexcart.product.repository.ProductRepository;
import com.nexcart.user.entity.User;
import com.nexcart.user.entity.UserRole;
import com.nexcart.user.repository.UserRepository;


import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;

import com.nexcart.product.specification.ProductSpecification;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository) {

        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public ProductResponseDTO createProduct(String email, ProductRequestDTO request) {

        User seller = resolveUser(email);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found!"));

        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setStock(request.getStock());
        product.setStatus(request.getStatus());
        product.setCategory(category);
        product.setSeller(seller);

        Product savedProduct = productRepository.save(product);

        return toResponseDTO(savedProduct);
    }

    public ProductResponseDTO getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found!"));

        return toResponseDTO(product);
    }

public Page<ProductResponseDTO> searchProducts(
        String name,
        Long categoryId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Pageable pageable) {

    Specification<Product> specification = Specification.unrestricted();

    if (name != null && !name.isBlank()) {
        specification = specification.and(
                ProductSpecification.hasName(name)
        );
    }

    if (categoryId != null) {
        specification = specification.and(
                ProductSpecification.hasCategory(categoryId)
        );
    }

    if (minPrice != null) {
        specification = specification.and(
                ProductSpecification.priceGreaterThanOrEqualTo(minPrice)
        );
    }

    if (maxPrice != null) {
        specification = specification.and(
                ProductSpecification.priceLessThanOrEqualTo(maxPrice)
        );
    }

    return productRepository
            .findAll(specification, pageable)
            .map(this::toResponseDTO);
}

    public Page<ProductResponseDTO> getMyProducts(String email, Pageable pageable) {

        User seller = resolveUser(email);

        return productRepository
                .findBySellerId(seller.getId(), pageable)
                .map(this::toResponseDTO);
    }

    public ProductResponseDTO updateProduct(String email, Long id, ProductRequestDTO request) {

        User currentUser = resolveUser(email);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found!"));

        assertCanModify(currentUser, product);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found!"));

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setStock(request.getStock());
        product.setStatus(request.getStatus());
        product.setCategory(category);

        Product updatedProduct = productRepository.save(product);

        return toResponseDTO(updatedProduct);
    }

    public void deleteProduct(String email, Long id) {

        User currentUser = resolveUser(email);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found!"));

        assertCanModify(currentUser, product);

        productRepository.delete(product);
    }

    private User resolveUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found!"));
    }

    private void assertCanModify(User currentUser, Product product) {

        if (currentUser.getRole() == UserRole.ADMIN) {
            return;
        }

        if (product.getSeller() == null
                || !product.getSeller().getId().equals(currentUser.getId())) {

            throw new AccessDeniedException("Access denied!");
        }
    }

    private ProductResponseDTO toResponseDTO(Product product) {

        ProductResponseDTO response = new ProductResponseDTO();

        response.setId(product.getId());
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        response.setDescription(product.getDescription());
        response.setStock(product.getStock());
        response.setStatus(product.getStatus());
        response.setCategoryName(product.getCategory().getName());

        if (product.getSeller() != null) {
            response.setSellerId(product.getSeller().getId());
            response.setSellerName(product.getSeller().getName());
        }

        return response;
    }
}

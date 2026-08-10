package com.nexcart.product.service;

import org.springframework.stereotype.Service;

import com.nexcart.category.entity.Category;
import com.nexcart.category.repository.CategoryRepository;
import com.nexcart.exception.CategoryNotFoundException;
import com.nexcart.product.dto.ProductRequestDTO;
import com.nexcart.product.dto.ProductResponseDTO;
import com.nexcart.product.entity.Product;
import com.nexcart.product.repository.ProductRepository;
import com.nexcart.exception.ProductNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class ProductService {
    

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public ProductResponseDTO createProduct(ProductRequestDTO request){
         
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found!"));

        Product product = new Product();

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setStock(request.getStock());
        product.setStatus(request.getStatus());

        product.setCategory(category);

        Product savedProduct = productRepository.save(product);

        ProductResponseDTO response = new ProductResponseDTO();

        response.setId(savedProduct.getId());
        response.setName(savedProduct.getName());
        response.setPrice(savedProduct.getPrice());
        response.setDescription(savedProduct.getDescription());
        response.setStock(savedProduct.getStock());
        response.setStatus(savedProduct.getStatus());

        response.setCategoryName(savedProduct.getCategory().getName());

        return response;
    }

    public ProductResponseDTO getProductById(Long id){

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found!"));

        ProductResponseDTO response = new ProductResponseDTO();

        response.setId(product.getId());
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        response.setDescription(product.getDescription());
        response.setStock(product.getStock());
        response.setStatus(product.getStatus());
        response.setCategoryName(product.getCategory().getName());

        return response;
    }

    public Page<ProductResponseDTO> getAllProducts(Pageable pageable){

        Page<Product> products = productRepository.findAll(pageable);

        return products.map(product -> {

            ProductResponseDTO response = new ProductResponseDTO();

            response.setId(product.getId());
            response.setName(product.getName());
            response.setPrice(product.getPrice());
            response.setDescription(product.getDescription());
            response.setStock(product.getStock());
            response.setStatus(product.getStatus());
            response.setCategoryName(product.getCategory().getName());

            return response;
        });
        
    }

    public ProductResponseDTO updateProduct(Long id,ProductRequestDTO request){

        Product product = productRepository.findById(id)
                .orElseThrow(()->
                      new ProductNotFoundException("Product not found!"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found!"));

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setStock(request.getStock());
        product.setStatus(request.getStatus());
        product.setCategory(category);

        Product updatedProduct = productRepository.save(product);

        ProductResponseDTO response = new ProductResponseDTO();

        response.setId(updatedProduct.getId());
        response.setName(updatedProduct.getName());
        response.setPrice(updatedProduct.getPrice());
        response.setDescription(updatedProduct.getDescription());
        response.setStock(updatedProduct.getStock());
        response.setStatus(updatedProduct.getStatus());
        response.setCategoryName(updatedProduct.getCategory().getName());

        return response;
    }

    public void deleteProduct(Long id){

        Product product = productRepository.findById(id)
                .orElseThrow(()->
                      new ProductNotFoundException("Product not found!"));

        productRepository.delete(product);
    }
}

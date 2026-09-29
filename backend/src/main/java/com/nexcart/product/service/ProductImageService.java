package com.nexcart.product.service;

import java.util.Map;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nexcart.exception.BadRequestException;
import com.nexcart.exception.ProductNotFoundException;
import com.nexcart.exception.ResourceNotFoundException;
import com.nexcart.product.dto.ProductImageResponseDTO;
import com.nexcart.product.entity.Product;
import com.nexcart.product.entity.ProductImage;
import com.nexcart.product.repository.ProductImageRepository;
import com.nexcart.product.repository.ProductRepository;
import com.nexcart.user.entity.User;
import com.nexcart.user.entity.UserRole;
import com.nexcart.user.repository.UserRepository;

@Service
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public ProductImageService(
            ProductImageRepository productImageRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            CloudinaryService cloudinaryService) {

        this.productImageRepository = productImageRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public ProductImageResponseDTO addImage(
            String email,
            Long productId,
            MultipartFile file) {

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found!"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found!"));

        if (currentUser.getRole() != UserRole.ADMIN
                && (product.getSeller() == null
                || !product.getSeller().getId().equals(currentUser.getId()))) {

            throw new AccessDeniedException("Access denied!");
        }

        if (file.isEmpty()) {
            throw new BadRequestException("Image file cannot be empty!");
        }

        if (file.getContentType() == null
                || !file.getContentType().startsWith("image/")) {

            throw new BadRequestException(
                    "Only image files are allowed!"
            );
        }

        Map<String, Object> uploadResult =
                cloudinaryService.upload(file);

        ProductImage productImage = new ProductImage();

        productImage.setImageUrl(
                (String) uploadResult.get("secure_url")
        );

        productImage.setPublicId(
                (String) uploadResult.get("public_id")
        );

        productImage.setProduct(product);

        ProductImage savedImage =
                productImageRepository.save(productImage);

        return new ProductImageResponseDTO(
                savedImage.getId(),
                savedImage.getImageUrl(),
                product.getId()
        );
    }

    public void deleteImage(
            String email,
            Long productId,
            Long imageId) {

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found!"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found!"));

        ProductImage productImage = productImageRepository.findById(imageId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product image not found!"
                        ));

        if (!productImage.getProduct().getId().equals(product.getId())) {

            throw new ResourceNotFoundException(
                    "Product image not found!"
            );
        }

        if (currentUser.getRole() != UserRole.ADMIN
                && (product.getSeller() == null
                || !product.getSeller().getId().equals(currentUser.getId()))) {

            throw new AccessDeniedException("Access denied!");
        }

        cloudinaryService.delete(productImage.getPublicId());

        productImageRepository.delete(productImage);
    }
}
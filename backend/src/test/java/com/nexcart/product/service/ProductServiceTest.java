package com.nexcart.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import com.nexcart.category.entity.Category;
import com.nexcart.category.repository.CategoryRepository;
import com.nexcart.product.dto.ProductRequestDTO;
import com.nexcart.product.dto.ProductResponseDTO;
import com.nexcart.product.entity.Product;
import com.nexcart.product.enums.ProductStatus;
import com.nexcart.product.repository.ProductRepository;
import com.nexcart.user.entity.User;
import com.nexcart.user.entity.UserRole;
import com.nexcart.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProductService productService;

    private User seller;
    private User otherUser;
    private User admin;
    private Category category;
    private ProductRequestDTO request;
    private Product ownedProduct;

    @BeforeEach
    void setUp() {
        seller = user(1L, "seller@test.com", "Seller One", UserRole.USER);
        otherUser = user(2L, "other@test.com", "Other User", UserRole.USER);
        admin = user(3L, "admin@test.com", "Admin User", UserRole.ADMIN);

        category = new Category("Books", "Used books");
        category.setId(10L);

        request = new ProductRequestDTO(
                "Calculus Textbook",
                new BigDecimal("25.00"),
                "Good condition",
                1,
                ProductStatus.ACTIVE,
                10L
        );

        ownedProduct = product(100L, seller);
    }

    @Test
    void createProduct_assignsSellerFromAuthenticatedIdentity() {
        when(userRepository.findByEmail("seller@test.com")).thenReturn(Optional.of(seller));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        ProductResponseDTO response = productService.createProduct("seller@test.com", request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());

        Product savedProduct = captor.getValue();
        assertThat(savedProduct.getSeller()).isEqualTo(seller);
        assertThat(response.getSellerId()).isEqualTo(1L);
        assertThat(response.getSellerName()).isEqualTo("Seller One");
        assertThat(response.getName()).isEqualTo("Calculus Textbook");
    }

    @Test
    void createProduct_adminBecomesSellerFromAuthenticatedIdentity() {
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            saved.setId(101L);
            return saved;
        });

        ProductResponseDTO response = productService.createProduct("admin@test.com", request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());

        assertThat(captor.getValue().getSeller()).isEqualTo(admin);
        assertThat(response.getSellerId()).isEqualTo(3L);
        assertThat(response.getSellerName()).isEqualTo("Admin User");
    }

    @Test
    void updateProduct_ownProduct_succeeds() {
        when(userRepository.findByEmail("seller@test.com")).thenReturn(Optional.of(seller));
        when(productRepository.findById(100L)).thenReturn(Optional.of(ownedProduct));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponseDTO response =
                productService.updateProduct("seller@test.com", 100L, request);

        assertThat(response.getSellerId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Calculus Textbook");
        verify(productRepository).save(ownedProduct);
    }

    @Test
    void updateProduct_otherUsersProduct_throwsAccessDenied() {
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));
        when(productRepository.findById(100L)).thenReturn(Optional.of(ownedProduct));

        assertThatThrownBy(() ->
                productService.updateProduct("other@test.com", 100L, request))
                .isInstanceOf(AccessDeniedException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_adminCanUpdateAnyProduct() {
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(productRepository.findById(100L)).thenReturn(Optional.of(ownedProduct));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponseDTO response =
                productService.updateProduct("admin@test.com", 100L, request);

        assertThat(response.getSellerId()).isEqualTo(1L);
        verify(productRepository).save(ownedProduct);
    }

    @Test
    void deleteProduct_ownProduct_succeeds() {
        when(userRepository.findByEmail("seller@test.com")).thenReturn(Optional.of(seller));
        when(productRepository.findById(100L)).thenReturn(Optional.of(ownedProduct));

        productService.deleteProduct("seller@test.com", 100L);

        verify(productRepository).delete(ownedProduct);
    }

    @Test
    void deleteProduct_otherUsersProduct_throwsAccessDenied() {
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));
        when(productRepository.findById(100L)).thenReturn(Optional.of(ownedProduct));

        assertThatThrownBy(() ->
                productService.deleteProduct("other@test.com", 100L))
                .isInstanceOf(AccessDeniedException.class);

        verify(productRepository, never()).delete(any());
    }

    @Test
    void getMyProducts_returnsOnlyAuthenticatedUsersListings() {
        Product otherListing = product(200L, otherUser);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(ownedProduct));

        when(userRepository.findByEmail("seller@test.com")).thenReturn(Optional.of(seller));
        when(productRepository.findBySellerId(eq(1L), eq(pageable))).thenReturn(page);

        Page<ProductResponseDTO> result =
                productService.getMyProducts("seller@test.com", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(100L);
        assertThat(result.getContent().get(0).getSellerId()).isEqualTo(1L);
        verify(productRepository).findBySellerId(1L, pageable);
        verify(productRepository, never()).findBySellerId(otherListing.getSeller().getId(), pageable);
    }

    @Test
    void getMyProducts_supportsPagination() {
        Pageable pageable = PageRequest.of(1, 5);
        Page<Product> page = new PageImpl<>(List.of(), pageable, 0);

        when(userRepository.findByEmail("seller@test.com")).thenReturn(Optional.of(seller));
        when(productRepository.findBySellerId(1L, pageable)).thenReturn(page);

        Page<ProductResponseDTO> result =
                productService.getMyProducts("seller@test.com", pageable);

        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(5);
    }

    @Test
    void getProductById_stillWorksAndIncludesSellerInfo() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(ownedProduct));

        ProductResponseDTO response = productService.getProductById(100L);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getSellerId()).isEqualTo(1L);
        assertThat(response.getSellerName()).isEqualTo("Seller One");
        assertThat(response.getCategoryName()).isEqualTo("Books");
    }

    private User user(Long id, String email, String name, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setName(name);
        user.setRole(role);
        return user;
    }

    private Product product(Long id, User seller) {
        Product product = new Product();
        product.setId(id);
        product.setName("Calculus Textbook");
        product.setPrice(new BigDecimal("25.00"));
        product.setDescription("Good condition");
        product.setStock(1);
        product.setStatus(ProductStatus.ACTIVE);
        product.setCategory(category);
        product.setSeller(seller);
        return product;
    }
}

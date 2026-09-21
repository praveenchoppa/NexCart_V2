package com.nexcart.product.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexcart.category.entity.Category;
import com.nexcart.category.repository.CategoryRepository;
import com.nexcart.product.dto.ProductRequestDTO;
import com.nexcart.product.entity.Product;
import com.nexcart.product.enums.ProductStatus;
import com.nexcart.product.repository.ProductRepository;
import com.nexcart.user.entity.User;
import com.nexcart.user.entity.UserRole;
import com.nexcart.user.repository.UserRepository;

@SpringBootTest
@Transactional
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User seller;
    private User otherUser;
    private Category category;
    private Product sellerProduct;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        seller = saveUser("seller@test.com", "Seller One", UserRole.USER);
        otherUser = saveUser("other@test.com", "Other User", UserRole.USER);

        category = categoryRepository.save(new Category("Books", "Used books"));

        sellerProduct = new Product();
        sellerProduct.setName("Seller Listing");
        sellerProduct.setPrice(new BigDecimal("25.00"));
        sellerProduct.setDescription("Good condition");
        sellerProduct.setStock(1);
        sellerProduct.setStatus(ProductStatus.ACTIVE);
        sellerProduct.setCategory(category);
        sellerProduct.setSeller(seller);
        sellerProduct = productRepository.save(sellerProduct);
    }

    @Test
    @WithMockUser(username = "seller@test.com", roles = "USER")
    void createProduct_assignsAuthenticatedUserAsSeller() throws Exception {
        ProductRequestDTO request = new ProductRequestDTO(
                "Desk Lamp",
                new BigDecimal("15.00"),
                "Working lamp",
                1,
                ProductStatus.ACTIVE,
                category.getId()
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Desk Lamp"))
                .andExpect(jsonPath("$.sellerId").value(seller.getId()))
                .andExpect(jsonPath("$.sellerName").value("Seller One"));
    }

    @Test
    @WithMockUser(username = "seller@test.com", roles = "USER")
    void updateOwnProduct_succeeds() throws Exception {
        ProductRequestDTO request = new ProductRequestDTO(
                "Updated Listing",
                new BigDecimal("30.00"),
                "Updated",
                2,
                ProductStatus.ACTIVE,
                category.getId()
        );

        mockMvc.perform(put("/api/products/" + sellerProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Listing"))
                .andExpect(jsonPath("$.sellerId").value(seller.getId()));
    }

    @Test
    @WithMockUser(username = "other@test.com", roles = "USER")
    void updateAnotherUsersProduct_returns403() throws Exception {
        ProductRequestDTO request = new ProductRequestDTO(
                "Stolen Update",
                new BigDecimal("30.00"),
                "Updated",
                2,
                ProductStatus.ACTIVE,
                category.getId()
        );

        mockMvc.perform(put("/api/products/" + sellerProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied!"));
    }

    @Test
    @WithMockUser(username = "seller@test.com", roles = "USER")
    void deleteOwnProduct_succeeds() throws Exception {
        mockMvc.perform(delete("/api/products/" + sellerProduct.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "other@test.com", roles = "USER")
    void deleteAnotherUsersProduct_returns403() throws Exception {
        mockMvc.perform(delete("/api/products/" + sellerProduct.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "seller@test.com", roles = "USER")
    void getMyProducts_returnsOnlyAuthenticatedUsersListings() throws Exception {
        Product otherListing = new Product();
        otherListing.setName("Other Listing");
        otherListing.setPrice(new BigDecimal("10.00"));
        otherListing.setStock(1);
        otherListing.setStatus(ProductStatus.ACTIVE);
        otherListing.setCategory(category);
        otherListing.setSeller(otherUser);
        productRepository.save(otherListing);

        mockMvc.perform(get("/api/products/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(sellerProduct.getId()))
                .andExpect(jsonPath("$.content[0].sellerId").value(seller.getId()));
    }

    @Test
    @WithMockUser(username = "buyer@test.com", roles = "USER")
    void getProductById_stillWorksAndIncludesSellerInfo() throws Exception {
        saveUser("buyer@test.com", "Buyer", UserRole.USER);

        mockMvc.perform(get("/api/products/" + sellerProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sellerProduct.getId()))
                .andExpect(jsonPath("$.sellerId").value(seller.getId()))
                .andExpect(jsonPath("$.sellerName").value("Seller One"));
    }

    private User saveUser(String email, String name, UserRole role) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(role);
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setUpdatedAt(java.time.LocalDateTime.now());
        return userRepository.save(user);
    }
}

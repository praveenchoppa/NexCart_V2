package com.nexcart.messaging.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
import com.nexcart.messaging.dto.MessageCreateRequestDTO;
import com.nexcart.messaging.entity.Conversation;
import com.nexcart.messaging.entity.Message;
import com.nexcart.messaging.repository.ConversationRepository;
import com.nexcart.messaging.repository.MessageRepository;
import com.nexcart.product.entity.Product;
import com.nexcart.product.enums.ProductStatus;
import com.nexcart.product.repository.ProductRepository;
import com.nexcart.user.entity.User;
import com.nexcart.user.entity.UserRole;
import com.nexcart.user.repository.UserRepository;

@SpringBootTest
@Transactional
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Conversation conversation;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        User buyer = saveUser("rahul@nexcart.com", "Rahul");
        User seller = saveUser("praveen@nexcart.com", "Praveen");
        saveUser("arjun@nexcart.com", "Arjun");

        Category category = categoryRepository.save(new Category("Books", "Used books"));

        Product product = new Product();
        product.setName("Textbook");
        product.setPrice(new BigDecimal("25.00"));
        product.setStock(1);
        product.setStatus(ProductStatus.ACTIVE);
        product.setCategory(category);
        product.setSeller(seller);
        product = productRepository.save(product);

        conversation = new Conversation();
        conversation.setBuyer(buyer);
        conversation.setSeller(seller);
        conversation.setProduct(product);
        conversation = conversationRepository.save(conversation);

        Message existing = new Message();
        existing.setConversation(conversation);
        existing.setSender(buyer);
        existing.setContent("Persisted history");
        existing.setCreatedAt(LocalDateTime.now());
        messageRepository.save(existing);
    }

    @Test
    @WithMockUser(username = "rahul@nexcart.com", roles = "USER")
    void getMessages_participant_returnsHistory() throws Exception {
        mockMvc.perform(get("/api/conversations/" + conversation.getId() + "/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].content").value("Persisted history"));
    }

    @Test
    @WithMockUser(username = "rahul@nexcart.com", roles = "USER")
    void getMessages_pagination_returnsPageMetadata() throws Exception {
        User buyer = userRepository.findByEmail("rahul@nexcart.com").orElseThrow();
        Message second = new Message();
        second.setConversation(conversation);
        second.setSender(buyer);
        second.setContent("Second page item");
        second.setCreatedAt(LocalDateTime.now().plusSeconds(1));
        messageRepository.save(second);

        mockMvc.perform(get("/api/conversations/" + conversation.getId() + "/messages")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(1));
    }

    @Test
    @WithMockUser(username = "arjun@nexcart.com", roles = "USER")
    void getMessages_nonParticipant_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/conversations/" + conversation.getId() + "/messages"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("You are not a participant in this conversation!"));
    }

    @Test
    @WithMockUser(username = "rahul@nexcart.com", roles = "USER")
    void getMessages_missingConversation_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/conversations/99999/messages"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Conversation not found!"));
    }

    @Test
    @WithMockUser(username = "rahul@nexcart.com", roles = "USER")
    void postMessage_participant_createsMessage() throws Exception {
        MessageCreateRequestDTO request = new MessageCreateRequestDTO();
        request.setContent("REST hello");

        mockMvc.perform(post("/api/conversations/" + conversation.getId() + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("REST hello"))
                .andExpect(jsonPath("$.senderName").value("Rahul"))
                .andExpect(jsonPath("$.conversationId").value(conversation.getId()));
    }

    @Test
    @WithMockUser(username = "arjun@nexcart.com", roles = "USER")
    void postMessage_nonParticipant_returnsForbidden() throws Exception {
        MessageCreateRequestDTO request = new MessageCreateRequestDTO();
        request.setContent("Not allowed");

        mockMvc.perform(post("/api/conversations/" + conversation.getId() + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("You are not a participant in this conversation!"));
    }

    @Test
    @WithMockUser(username = "rahul@nexcart.com", roles = "USER")
    void postMessage_blankContent_returnsBadRequest() throws Exception {
        MessageCreateRequestDTO request = new MessageCreateRequestDTO();
        request.setContent("   ");

        mockMvc.perform(post("/api/conversations/" + conversation.getId() + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private User saveUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(UserRole.USER);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}

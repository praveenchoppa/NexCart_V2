package com.nexcart.product.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_image")
@Getter
@Setter
@NoArgsConstructor
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String imageUrl;

    @Column(nullable = false, length = 500)
    private String publicId;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
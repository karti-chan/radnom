package com.example.radnom.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // ID elementu koszyka

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity = 1;

    // Pola kopiowane z produktu (opcjonalnie, dla wydajności)
    @Column(name = "product_name")
    private String productName;

    @Column(name = "price")
    private Integer price;

    @PrePersist
    @PreUpdate
    private void syncProductData() {
        if (product != null) {
            this.productName = product.getProductName();
            this.price = product.getPrice();
        }
    }
}
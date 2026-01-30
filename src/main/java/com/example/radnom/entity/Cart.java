package com.example.radnom.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Data  // Generuje: getId(), setId(), equals(), hashCode(), toString()
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Lombok wygeneruje getId() i setId()

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ========== TRANSIENT METHODS ==========

    @Transient
    public Integer getTotalPrice() {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        return items.stream()
                .mapToInt(item -> {
                    Integer price = item.getPrice();
                    Integer quantity = item.getQuantity();
                    return (price != null ? price * quantity : 0);
                })
                .sum();
    }

    @Transient
    public Integer getTotalItems() {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    // ========== BUSINESS METHODS ==========

    public void addItem(CartItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
        item.setCart(this);
        updatedAt = LocalDateTime.now();
    }

    public void removeItem(CartItem item) {
        if (items != null) {
            items.remove(item);
            item.setCart(null);
            updatedAt = LocalDateTime.now();
        }
    }

    public void clear() {
        if (items != null) {
            items.forEach(item -> item.setCart(null));
            items.clear();
            updatedAt = LocalDateTime.now();
        }
    }
}
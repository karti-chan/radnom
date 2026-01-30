package com.example.radnom.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
@Data  // ✅ TO WYSTARCZY - zawiera @Getter, @Setter, @ToString, @EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonIgnore
    private Cart cart;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "added_at")
    @Builder.Default
    private LocalDateTime addedAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ========== LIFECYCLE CALLBACKS ==========

    @PrePersist
    @PreUpdate
    private void syncProductData() {
        if (product != null) {
            this.productName = product.getProductName();
            this.price = product.getPrice();
            this.imageUrl = product.getImageUrl();
        }
        this.updatedAt = LocalDateTime.now();
    }

    // ========== TRANSIENT METHODS ==========

    @Transient
    public Integer getTotalPrice() {
        return price != null && quantity != null ? price * quantity : 0;
    }

    @Transient
    public Double getTotalPriceDouble() {
        return price != null && quantity != null ? price.doubleValue() * quantity : 0.0;
    }

    @Transient
    public String getFormattedTotalPrice() {
        return String.format("%.2f zł", getTotalPriceDouble());
    }

    @Transient
    public String getFormattedPrice() {
        return price != null ? String.format("%.2f zł", price.doubleValue()) : "0.00 zł";
    }

    // ========== BUSINESS METHODS ==========

    public void increaseQuantity(Integer amount) {
        if (amount != null && amount > 0) {
            this.quantity += amount;
        }
    }

    public void decreaseQuantity(Integer amount) {
        if (amount != null && amount > 0) {
            this.quantity = Math.max(0, this.quantity - amount);
        }
    }

    public void updatePriceFromProduct() {
        if (product != null && product.getPrice() != null) {
            this.price = product.getPrice();
        }
    }

    // ========== VALIDATION METHODS ==========

    @Transient
    public boolean isValid() {
        return product != null &&
                quantity != null && quantity > 0 &&
                price != null && price >= 0;
    }

    @Transient
    public boolean isOutOfStock(Integer availableStock) {
        return availableStock != null && quantity > availableStock;
    }

    // ✅ @Data daje toString(), ale możesz nadpisać jeśli chcesz custom:
    @Override
    public String toString() {
        return "CartItem{" +
                "id=" + id +
                ", productId=" + (product != null ? product.getId() : "null") +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", total=" + getTotalPrice() +
                ", addedAt=" + addedAt +
                '}';
    }
}
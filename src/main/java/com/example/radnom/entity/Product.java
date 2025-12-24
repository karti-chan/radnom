package com.example.radnom.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;

@Entity
@Table(name = "radnom")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    // ❌ USUŃ TĘ LINIĘ:
    // @GeneratedValue(strategy = GenerationType.IDENTITY)

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "product_id")        
    private Integer productId;
    
    @Column(name = "product_name")      
    private String productName;
    
    @Column(name = "price")            
    private Integer price;
    
    @Column(name = "product_date")      
    private String productDate;
    
//NOWE POLA DLA STRONY PRODUKTU
    
    @Column(name = "description", length = 1000)
    private String description = "Brak opisu produktu";
    
    @Column(name = "category")
    private String category = "Inne";
    
    @Column(name = "image_url")
    private String imageUrl = "/images/default-product.jpg";
    
    @Column(name = "stock")
    private Integer stock = 0;
    
    @Column(name = "weight")
    private Double weight;
    
    @Column(name = "dimensions")
    private String dimensions;
    
    @Column(name = "brand")
    private String brand;
    
    @Column(name = "rating")
    private Double rating = 0.0;
    
    @Column(name = "review_count")
    private Integer reviewCount = 0;
}
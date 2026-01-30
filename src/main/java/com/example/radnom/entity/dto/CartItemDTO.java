package com.example.radnom.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Integer id;
    private Integer productId;
    private String productName;
    private Integer quantity;
    private Integer price;
    private String formattedPrice;
    private String formattedTotalPrice;
    private String imageUrl;
    private Double totalPrice;
}
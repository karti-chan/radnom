package com.example.radnom.service.mapper;

import com.example.radnom.entity.dto.CartDTO;
import com.example.radnom.entity.dto.CartItemDTO;
import com.example.radnom.entity.Cart;
import com.example.radnom.entity.CartItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartDTO toDTO(Cart cart) {
        if (cart == null) {
            return null;
        }

        return CartDTO.builder()
                .cartId(cart.getId())
                .totalItems(cart.getTotalItemsCount())
                .totalPrice(cart.getTotalPrice())
                .formattedTotalPrice(cart.getFormattedTotalPrice())
                .items(mapItemsToDTO(cart.getItems()))
                .build();
    }

    public CartItemDTO toDTO(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }

        return CartItemDTO.builder()
                .id(cartItem.getId())
                .productId(cartItem.getProduct() != null ? cartItem.getProduct().getId() : null)
                .productName(cartItem.getProductName())
                .quantity(cartItem.getQuantity())
                .price(cartItem.getPrice())
                .formattedPrice(cartItem.getFormattedPrice())
                .formattedTotalPrice(cartItem.getFormattedTotalPrice())
                .imageUrl(cartItem.getImageUrl())
                .totalPrice(cartItem.getTotalPriceDouble())
                .build();
    }

    private List<CartItemDTO> mapItemsToDTO(List<CartItem> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
package com.example.radnom.service;

import com.example.radnom.entity.*;
import com.example.radnom.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public Cart getOrCreateCart(String userEmail) {
        return cartRepository.findByUserEmail(userEmail)
                .orElseGet(() -> {
                    User user = userRepository.findByEmail(userEmail)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    public Cart addToCart(String userEmail, Integer productId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Cart cart = getOrCreateCart(userEmail);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Sprawdź czy produkt już jest w koszyku
        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductProductId(cart.getId(), productId);

        if (existingItem.isPresent()) {
            // Zwiększ ilość jeśli już istnieje
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            // Dodaj nowy element
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setProductName(product.getProductName());
            newItem.setPrice(product.getPrice());
            cartItemRepository.save(newItem);
        }

        return cartRepository.findById(cart.getId())
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }

    public Cart removeFromCart(String userEmail, Integer productId) {
        Cart cart = getOrCreateCart(userEmail);

        CartItem item = cartItemRepository
                .findByCartIdAndProductProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        cartItemRepository.delete(item);
        return cartRepository.findById(cart.getId())
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }

    public Cart updateQuantity(String userEmail, Integer productId, Integer quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        Cart cart = getOrCreateCart(userEmail);

        if (quantity == 0) {
            // Usuń jeśli ilość = 0
            cartItemRepository.findByCartIdAndProductProductId(cart.getId(), productId)
                    .ifPresent(cartItemRepository::delete);
        } else {
            // Zaktualizuj ilość
            CartItem item = cartItemRepository
                    .findByCartIdAndProductProductId(cart.getId(), productId)
                    .orElseThrow(() -> new RuntimeException("Item not found in cart"));

            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return cartRepository.findById(cart.getId())
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }

    public void clearCart(String userEmail) {
        Cart cart = getOrCreateCart(userEmail);
        cartItemRepository.deleteByCartId(cart.getId());
    }

    public Integer getCartItemCount(String userEmail) {
        Cart cart = getOrCreateCart(userEmail);
        return cartItemRepository.countByCartId(cart.getId());
    }
}
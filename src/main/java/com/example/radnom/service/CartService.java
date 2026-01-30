package com.example.radnom.service;  // ✅ Zmieniłem na service

import com.example.radnom.entity.*;
import com.example.radnom.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartService {  // ✅ Zmieniłem nazwę z CartController na CartService

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // ========== PUBLIC API ==========

    public int getCartItemCount(String email) {
        log.debug("Calculating cart item count for user: {}", email);
        validateUserEmail(email);

        Cart cart = getOrCreateCart(email);

        // ✅ Lombok daje getId() dla Long id
        int count = cartItemRepository.countByCartId(cart.getId());
        return count;
    }

    public Cart getCart(String email) {
        log.debug("Getting cart for user: {}", email);
        validateUserEmail(email);

        return getOrCreateCart(email);
    }

    public List<CartItem> getCartItems(String email) {
        log.debug("Getting cart items for user: {}", email);
        validateUserEmail(email);

        Cart cart = getOrCreateCart(email);
        return cart.getItems();
    }

    public Cart addToCart(String email, Integer productId, Integer quantity) {
        log.debug("Adding product {} (quantity: {}) to cart for user: {}",
                productId, quantity, email);

        validateAddToCartRequest(email, productId, quantity);
        Cart cart = getOrCreateCart(email);
        Product product = getProductById(productId);

        // ✅ Lombok daje getId() dla Long id w Cart
        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId);

        if (existingItem.isPresent()) {
            updateExistingItem(existingItem.get(), quantity);
            cartItemRepository.save(existingItem.get());
        } else {
            CartItem newItem = createCartItem(cart, product, quantity);
            cartItemRepository.save(newItem);
            cart.getItems().add(newItem);
        }

        log.info("Successfully added product {} (quantity: {}) to cart for user {}",
                productId, quantity, email);

        return cart;
    }

    public Cart removeFromCart(String email, Integer productId) {
        log.debug("Removing product {} from cart for user: {}", productId, email);

        validateRemoveFromCartRequest(email, productId);
        Cart cart = getOrCreateCart(email);

        // ✅ Lombok daje getId() dla Long id w Cart
        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found in cart"));

        // ✅ Lombok daje getId() dla Long id w CartItem
        cartItemRepository.delete(item);
        cart.getItems().removeIf(ci -> ci.getId().equals(item.getId()));

        log.info("Successfully removed product {} from cart for user {}", productId, email);
        return cart;
    }

    public Cart updateQuantity(String email, Integer productId, Integer quantity) {
        log.debug("Updating product {} quantity to {} for user: {}",
                productId, quantity, email);

        validateUpdateQuantityRequest(email, productId, quantity);
        Cart cart = getOrCreateCart(email);

        // ✅ Lombok daje getId() dla Long id w Cart
        if (quantity == 0) {
            cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                    .ifPresent(item -> {
                        cartItemRepository.delete(item);
                        cart.getItems().removeIf(ci -> ci.getId().equals(item.getId()));
                    });
        } else {
            CartItem item = cartItemRepository
                    .findByCartIdAndProductId(cart.getId(), productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found in cart"));

            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        log.info("Updated quantity of product {} to {} for user {}", productId, quantity, email);
        return cart;
    }

    public void clearCart(String email) {
        log.debug("Clearing cart for user: {}", email);
        validateUserEmail(email);

        Cart cart = getOrCreateCart(email);

        // ✅ Lombok daje getId() dla Long id w Cart
        cartItemRepository.deleteByCartId(cart.getId());
        cart.getItems().clear();

        log.info("Cleared cart for user {}", email);
    }

    // ========== PRIVATE BUSINESS LOGIC ==========

    private Cart getOrCreateCart(String email) {
        return cartRepository.findByUserEmail(email)
                .orElseGet(() -> {
                    User user = getUserByEmail(email);
                    Cart newCart = createNewCart(user);
                    log.debug("Created new cart for user: {}", email);
                    return newCart;
                });
    }

    private CartItem createCartItem(Cart cart, Product product, Integer quantity) {
        return CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(quantity)
                .price(product.getPrice())
                .productName(product.getProductName())
                .build();
    }

    private void updateExistingItem(CartItem item, Integer additionalQuantity) {
        item.setQuantity(item.getQuantity() + additionalQuantity);
        log.debug("Increased quantity of product {} to {}",
                item.getProduct().getId(), item.getQuantity());
    }

    private Cart createNewCart(User user) {
        Cart newCart = Cart.builder()
                .user(user)
                .build();
        return cartRepository.save(newCart);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new IllegalArgumentException("User not found");
                });
    }

    private Product getProductById(Integer productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("Product not found with ID: {}", productId);
                    return new IllegalArgumentException("Product not found");
                });
    }

    // ========== VALIDATION METHODS ==========

    private void validateUserEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
    }

    private void validateAddToCartRequest(String email, Integer productId, Integer quantity) {
        validateUserEmail(email);

        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Invalid product ID");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
    }

    private void validateRemoveFromCartRequest(String email, Integer productId) {
        validateUserEmail(email);

        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Invalid product ID");
        }
    }

    private void validateUpdateQuantityRequest(String email, Integer productId, Integer quantity) {
        validateUserEmail(email);

        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Invalid product ID");
        }
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Quantity must be non-negative");
        }
    }

    // ========== ADDITIONAL BUSINESS METHODS ==========

    public double calculateCartTotal(String email) {
        log.debug("Calculating cart total for user: {}", email);
        validateUserEmail(email);

        Cart cart = getOrCreateCart(email);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return 0.0;
        }

        double total = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        log.debug("Cart total for user {}: {}", email, total);
        return total;
    }

    public boolean isProductInCart(String email, Integer productId) {
        validateUserEmail(email);

        if (productId == null || productId <= 0) {
            return false;
        }

        Cart cart = getOrCreateCart(email);
        // ✅ Lombok daje getId() dla Long id w Cart
        return cartItemRepository.existsByCartIdAndProductId(cart.getId(), productId);
    }
}
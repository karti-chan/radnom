package com.example.radnom.controller;

import com.example.radnom.entity.Cart;
import com.example.radnom.entity.CartItem;
import com.example.radnom.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:5173")
public class CartController {

    @Autowired
    private CartService cartService;

    // Pobierz koszyk zalogowanego użytkownika
    @GetMapping
    public ResponseEntity<Cart> getCart(Authentication authentication) {
        String email = authentication.getName();
        Cart cart = cartService.getOrCreateCart(email);
        return ResponseEntity.ok(cart);
    }

    // Pobierz elementy koszyka
    @GetMapping("/items")
    public ResponseEntity<List<CartItem>> getCartItems(Authentication authentication) {
        String email = authentication.getName();
        Cart cart = cartService.getOrCreateCart(email);
        return ResponseEntity.ok(cart.getItems());
    }

    // Dodaj produkt do koszyka
    @PostMapping("/add")
    public ResponseEntity<Cart> addToCart(
            @RequestParam Integer productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            Authentication authentication) {
        String email = authentication.getName();
        Cart cart = cartService.addToCart(email, productId, quantity);
        return ResponseEntity.ok(cart);
    }

    // Usuń produkt z koszyka
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Cart> removeFromCart(
            @PathVariable Integer productId,
            Authentication authentication) {
        String email = authentication.getName();
        Cart cart = cartService.removeFromCart(email, productId);
        return ResponseEntity.ok(cart);
    }

    // Zaktualizuj ilość produktu
    @PutMapping("/update")
    public ResponseEntity<Cart> updateQuantity(
            @RequestParam Integer productId,
            @RequestParam Integer quantity,
            Authentication authentication) {
        String email = authentication.getName();
        Cart cart = cartService.updateQuantity(email, productId, quantity);
        return ResponseEntity.ok(cart);
    }

    // Wyczyść cały koszyk
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        String email = authentication.getName();
        cartService.clearCart(email);
        return ResponseEntity.ok().build();
    }
}
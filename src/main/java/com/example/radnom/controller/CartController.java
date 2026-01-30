package com.example.radnom.controller;

import com.example.radnom.entity.Cart;
import com.example.radnom.entity.CartItem;
import com.example.radnom.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // 1. Pobierz liczbę produktów w koszyku
    @GetMapping("/count")
    public ResponseEntity<Integer> getCartItemCount(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername(); // email jest w username
        return ResponseEntity.ok(cartService.getCartItemCount(email));
    }

    // 2. Pobierz cały koszyk
    @GetMapping
    public ResponseEntity<Cart> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(cartService.getCart(email));
    }

    // 3. Pobierz produkty w koszyku
    @GetMapping("/items")
    public ResponseEntity<List<CartItem>> getCartItems(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(cartService.getCartItems(email));
    }

    // 4. Dodaj produkt do koszyka (teraz z RequestBody dla lepszej struktury)
    @PostMapping("/add")
    public ResponseEntity<Cart> addToCart(@AuthenticationPrincipal UserDetails userDetails,
                                          @RequestParam Integer productId,
                                          @RequestParam Integer quantity) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(cartService.addToCart(email, productId, quantity));
    }

    // 5. Usuń produkt z koszyka
    @DeleteMapping("/remove")
    public ResponseEntity<Cart> removeFromCart(@AuthenticationPrincipal UserDetails userDetails,
                                               @RequestParam Integer productId) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(cartService.removeFromCart(email, productId));
    }

    // 6. Zaktualizuj ilość produktu
    @PutMapping("/update")
    public ResponseEntity<Cart> updateQuantity(@AuthenticationPrincipal UserDetails userDetails,
                                               @RequestParam Integer productId,
                                               @RequestParam Integer quantity) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(cartService.updateQuantity(email, productId, quantity));
    }

    // 7. Wyczyść cały koszyk
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        cartService.clearCart(email);
        return ResponseEntity.ok().build();
    }
}
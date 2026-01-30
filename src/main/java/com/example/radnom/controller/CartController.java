package com.example.radnom.controller;

import com.example.radnom.entity.dto.CartDTO;
import com.example.radnom.entity.dto.CartItemDTO;
import com.example.radnom.service.mapper.CartMapper;
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
    private final CartMapper cartMapper;

    // 1. Pobierz koszyk jako DTO
    @GetMapping
    public ResponseEntity<CartDTO> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        var cart = cartService.getCart(email);
        return ResponseEntity.ok(cartMapper.toDTO(cart));
    }

    // 2. Pobierz liczbę produktów w koszyku
    @GetMapping("/count")
    public ResponseEntity<Integer> getCartItemCount(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(cartService.getCartItemCount(email));
    }

    // 3. Pobierz produkty w koszyku jako DTO
    @GetMapping("/items")
    public ResponseEntity<List<CartItemDTO>> getCartItems(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        var cart = cartService.getCart(email);
        return ResponseEntity.ok(
                cart.getItems().stream()
                        .map(cartMapper::toDTO)
                        .toList()
        );
    }

    // 4. Dodaj produkt do koszyka
    @PostMapping("/add")
    public ResponseEntity<CartDTO> addToCart(@AuthenticationPrincipal UserDetails userDetails,
                                             @RequestParam Integer productId,
                                             @RequestParam(defaultValue = "1") Integer quantity) {
        String email = userDetails.getUsername();
        var cart = cartService.addToCart(email, productId, quantity);
        return ResponseEntity.ok(cartMapper.toDTO(cart));
    }

    // 5. Usuń produkt z koszyka (z @RequestParam)
    @DeleteMapping("/remove")
    public ResponseEntity<CartDTO> removeFromCart(@AuthenticationPrincipal UserDetails userDetails,
                                                  @RequestParam Integer productId) {
        String email = userDetails.getUsername();
        var cart = cartService.removeFromCart(email, productId);
        return ResponseEntity.ok(cartMapper.toDTO(cart));
    }

    // ALBO z @PathVariable (wybierz jedną wersję)
    /*
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<CartDTO> removeFromCart(@AuthenticationPrincipal UserDetails userDetails,
                                                  @PathVariable Integer productId) {
        String email = userDetails.getUsername();
        var cart = cartService.removeFromCart(email, productId);
        return ResponseEntity.ok(cartMapper.toDTO(cart));
    }
    */

    // 6. Zaktualizuj ilość produktu
    @PutMapping("/update")
    public ResponseEntity<CartDTO> updateQuantity(@AuthenticationPrincipal UserDetails userDetails,
                                                  @RequestParam Integer productId,
                                                  @RequestParam Integer quantity) {
        String email = userDetails.getUsername();
        var cart = cartService.updateQuantity(email, productId, quantity);
        return ResponseEntity.ok(cartMapper.toDTO(cart));
    }

    // 7. Wyczyść cały koszyk
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        cartService.clearCart(email);
        return ResponseEntity.ok().build();
    }

    // 8. Pobierz sumę koszyka
    @GetMapping("/total")
    public ResponseEntity<Double> getCartTotal(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(cartService.calculateCartTotal(email));
    }

    // 9. Sprawdź czy produkt jest w koszyku
    @GetMapping("/contains/{productId}")
    public ResponseEntity<Boolean> isProductInCart(@AuthenticationPrincipal UserDetails userDetails,
                                                   @PathVariable Integer productId) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(cartService.isProductInCart(email, productId));
    }
}
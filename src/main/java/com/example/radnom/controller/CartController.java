package com.example.radnom.controller;

import com.example.radnom.entity.CartItem;
import com.example.radnom.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:5173")
public class CartController {
    
    @Autowired
    private CartRepository cartRepository;
    
    @GetMapping
    public List<CartItem> getCartItems() {
        return cartRepository.findAll();
    }
    
    @PostMapping
    public CartItem addToCart(@RequestBody CartItem cartItem) {
        return cartRepository.save(cartItem);
    }
    
    @DeleteMapping("/{id}")
    public void removeFromCart(@PathVariable Long id) {
        cartRepository.deleteById(id);
    }
    
    @PutMapping("/{id}")
    public CartItem updateQuantity(@PathVariable Long id, @RequestBody CartItem cartItem) {
        cartItem.setId(id);
        return cartRepository.save(cartItem);
    }
}
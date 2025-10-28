package com.example.radnom.controller;

import com.example.radnom.entity.Product;
import com.example.radnom.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:3000")
public class ProductController {
    
    @Autowired
    private ProductRepository productRepository;
    
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    // DODAJ TĘ METODĘ ↓
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Integer id) {
        Optional<Product> product = productRepository.findById(id);
        
        if (product.isPresent()) {
            return product.get();
        } else {
            throw new RuntimeException("Product not found with id: " + id);
        }
    }
}
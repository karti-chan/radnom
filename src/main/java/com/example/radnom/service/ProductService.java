package com.example.radnom.service;

import com.example.radnom.entity.Product;
import com.example.radnom.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        log.info("Getting all products");
        return productRepository.findAll();
    }

    public Product getProductById(Integer id) {
        log.info("Getting product by id: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public List<Product> getProductsByCategory(String category) {
        log.info("Getting products by category: {}", category);
        return productRepository.findByCategory(category);
    }

    public List<Product> getSortedProducts(String sortType) {
        log.info("Getting sorted products by: {}", sortType);
        List<Product> products = productRepository.findAll();

        switch (sortType.toLowerCase()) {
            case "price-asc":
                products.sort(Comparator.comparing(Product::getPrice));
                break;
            case "price-desc":
                products.sort(Comparator.comparing(Product::getPrice).reversed());
                break;
            case "name-asc":
                products.sort(Comparator.comparing(Product::getProductName, String.CASE_INSENSITIVE_ORDER));
                break;
            case "name-desc":
                products.sort(Comparator.comparing(Product::getProductName, String.CASE_INSENSITIVE_ORDER).reversed());
                break;
        }

        return products;
    }

    public List<Product> getProductsByPriceRange(Integer minPrice, Integer maxPrice) {
        log.info("Filtering products by price range: {} - {}", minPrice, maxPrice);

        return productRepository.findAll().stream()
                .filter(p -> {
                    boolean passMin = (minPrice == null) || (p.getPrice() >= minPrice);
                    boolean passMax = (maxPrice == null) || (p.getPrice() <= maxPrice);
                    return passMin && passMax;
                })
                .toList();
    }
}
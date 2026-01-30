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

    // ========== EXISTING METHODS (zachowaj te) ==========

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

    // ========== NEW SEARCH METHODS (dodaj te) ==========

    // ✅ WYSZUKIWARKA PRODUKTÓW - podstawowa
    public List<Product> searchProducts(String query) {
        log.info("Searching products with query: '{}'", query);

        if (query == null || query.trim().isEmpty()) {
            return getAllProducts(); // zwróć wszystkie jeśli puste
        }

        String searchQuery = query.trim().toLowerCase();
        return productRepository.searchByNameOrDescription(searchQuery);
    }

    // ✅ WYSZUKIWANIE PO KATEGORII
    public List<Product> searchByCategory(String category) {
        log.info("Searching products in category: '{}'", category);
        if (category == null || category.trim().isEmpty()) {
            return getAllProducts();
        }
        return productRepository.searchByCategory(category.toLowerCase().trim());
    }

    // ✅ WYSZUKIWANIE PO NAZWIE
    public List<Product> searchByName(String name) {
        log.info("Searching products by name: '{}'", name);
        if (name == null || name.trim().isEmpty()) {
            return getAllProducts();
        }
        return productRepository.searchByName(name.trim().toLowerCase());
    }

    // ✅ WYSZUKIWANIE ZAAWANSOWANE (nazwa, opis lub kategoria)
    public List<Product> advancedSearch(String query) {
        log.info("Advanced search with query: '{}'", query);

        if (query == null || query.trim().isEmpty()) {
            return getAllProducts();
        }

        String searchQuery = query.trim().toLowerCase();

        // Jeśli masz metodę searchByNameOrDescriptionOrCategory w repo, użyj jej:
        // return productRepository.searchByNameOrDescriptionOrCategory(searchQuery);

        // Albo zrób ręcznie:
        return productRepository.findAll().stream()
                .filter(product ->
                        (product.getProductName() != null &&
                                product.getProductName().toLowerCase().contains(searchQuery)) ||
                                (product.getDescription() != null &&
                                        product.getDescription().toLowerCase().contains(searchQuery)) ||
                                (product.getCategory() != null &&
                                        product.getCategory().toLowerCase().contains(searchQuery))
                )
                .toList();
    }

    // ✅ WYSZUKIWANIE Z FILTREM CENY
    public List<Product> searchProductsWithPriceFilter(String query, Integer minPrice, Integer maxPrice) {
        log.info("Searching products with query '{}' and price range: {}-{}",
                query, minPrice, maxPrice);

        List<Product> products;

        if (query == null || query.trim().isEmpty()) {
            // Jeśli brak query, użyj filtru ceny na wszystkich produktach
            products = getAllProducts();
        } else {
            // Najpierw wyszukaj po query
            products = searchProducts(query);
        }

        // Potem zastosuj filtr ceny
        return products.stream()
                .filter(p -> {
                    boolean passMin = (minPrice == null) || (p.getPrice() >= minPrice);
                    boolean passMax = (maxPrice == null) || (p.getPrice() <= maxPrice);
                    return passMin && passMax;
                })
                .toList();
    }

    // ✅ POBRANIE WSZYSTKICH UNIKALNYCH KATEGORII
    public List<String> getAllCategories() {
        log.info("Getting all unique categories");

        // Jeśli masz metodę w repo:
        // return productRepository.findAllDistinctCategories();

        // Albo ręcznie:
        return productRepository.findAll().stream()
                .map(Product::getCategory)
                .distinct()
                .filter(category -> category != null && !category.trim().isEmpty())
                .sorted()
                .toList();
    }

    // ✅ POBRANIE NAJPOPULARNIEJSZYCH PRODUKTÓW (możesz dodać logikę)
    public List<Product> getPopularProducts(int limit) {
        log.info("Getting {} popular products", limit);

        // Na razie zwróć pierwsze X produktów
        // Możesz później dodać logikę z liczbą zamówień/odwiedzin
        return productRepository.findAll().stream()
                .limit(limit)
                .toList();
    }

    // ✅ POBRANIE NOWYCH PRODUKTÓW (jeśli masz pole createdAt)
    public List<Product> getNewProducts(int limit) {
        log.info("Getting {} new products", limit);

        // Jeśli masz pole createdAt w Product, możesz sortować po nim:
        // return productRepository.findAllByOrderByCreatedAtDesc()
        //         .stream().limit(limit).toList();

        // Na razie zwróć wszystkie
        return productRepository.findAll().stream()
                .limit(limit)
                .toList();
    }
}
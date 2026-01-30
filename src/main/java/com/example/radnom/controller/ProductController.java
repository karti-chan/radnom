package com.example.radnom.controller;

import com.example.radnom.entity.dto.PriceFilterDTO;
import com.example.radnom.entity.Product;
import com.example.radnom.service.ProductService;
import com.example.radnom.entity.dto.QuickSearchResultDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.origin:http://localhost:5173}")
public class ProductController {

    private final ProductService productService;

    // ========== EXISTING ENDPOINTS ==========

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        log.info("GET /api/products - returning all products");
        try {
            List<Product> products = productService.getAllProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error getting products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Integer id) {
        log.info("GET /api/products/{} - searching...", id);

        try {
            Product product = productService.getProductById(id);
            log.info("Product found: ID={}, Name={}, Price={}",
                    product.getProductId(), product.getProductName(), product.getPrice());
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            log.warn("Product not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error getting product by id: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        log.info("GET /api/products/category/{}", category);

        try {
            List<Product> products = productService.getProductsByCategory(category);
            log.info("Found {} products in category: {}", products.size(), category);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error getting products by category: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/sorted/{sortType}")
    public ResponseEntity<List<Product>> getSortedProducts(@PathVariable String sortType) {
        log.info("GET /api/products/sorted/{}", sortType);

        try {
            List<Product> products = productService.getSortedProducts(sortType);
            log.info("Returning {} sorted products", products.size());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error sorting products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/filter/price")
    public ResponseEntity<List<Product>> getProductsByPriceRange(
            @RequestBody PriceFilterDTO filterDTO) {

        log.info("POST /api/products/filter/price - min: {}, max: {}",
                filterDTO.getMinPrice(), filterDTO.getMaxPrice());

        try {
            List<Product> filteredProducts = productService.getProductsByPriceRange(
                    filterDTO.getMinPrice(), filterDTO.getMaxPrice());

            log.info("Found {} products in price range: {}-{}",
                    filteredProducts.size(), filterDTO.getMinPrice(), filterDTO.getMaxPrice());
            return ResponseEntity.ok(filteredProducts);
        } catch (Exception e) {
            log.error("Error filtering by price: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/filter/price-old")
    public ResponseEntity<List<Product>> getProductsByPriceRangeOld(
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice) {

        log.info("GET /api/products/filter/price-old?min={}&max={}", minPrice, maxPrice);

        try {
            List<Product> filteredProducts = productService.getProductsByPriceRange(minPrice, maxPrice);
            return ResponseEntity.ok(filteredProducts);
        } catch (Exception e) {
            log.error("Error filtering by price: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== SEARCH ENDPOINTS ==========

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam(required = false) String q) {
        log.info("GET /api/products/search?q={}", q);

        try {
            List<Product> products = productService.searchProducts(q);
            log.info("Found {} products for query: '{}'", products.size(), q);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error searching products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search/advanced")
    public ResponseEntity<List<Product>> advancedSearch(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "name-asc") String sort) {

        log.info("GET /api/products/search/advanced?query={}&minPrice={}&maxPrice={}&category={}&sort={}",
                query, minPrice, maxPrice, category, sort);

        try {
            List<Product> products;

            // Najpierw wyszukaj po query
            if (query != null && !query.trim().isEmpty()) {
                products = productService.searchProducts(query);
            } else {
                products = productService.getAllProducts();
            }

            // Filtruj po kategorii jeśli podana
            if (category != null && !category.trim().isEmpty()) {
                products = products.stream()
                        .filter(p -> p.getCategory() != null &&
                                p.getCategory().equalsIgnoreCase(category))
                        .toList();
            }

            // Filtruj po cenie jeśli podana
            if (minPrice != null || maxPrice != null) {
                products = products.stream()
                        .filter(p -> {
                            boolean passMin = (minPrice == null) || (p.getPrice() >= minPrice);
                            boolean passMax = (maxPrice == null) || (p.getPrice() <= maxPrice);
                            return passMin && passMax;
                        })
                        .toList();
            }

            // Sortuj jeśli podano
            if (sort != null) {
                products = productService.getSortedProducts(sort);
            }

            log.info("Advanced search found {} products", products.size());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error in advanced search: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/search")
    public ResponseEntity<List<Product>> searchProductsPost(@RequestBody SearchRequest request) {
        log.info("POST /api/products/search - query: '{}'", request.getQuery());

        try {
            List<Product> products = productService.searchProducts(request.getQuery());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error searching products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search/name")
    public ResponseEntity<List<Product>> searchByName(@RequestParam String name) {
        log.info("GET /api/products/search/name?name={}", name);

        try {
            List<Product> products = productService.searchByName(name);
            log.info("Found {} products with name containing: '{}'", products.size(), name);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error searching by name: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        log.info("GET /api/products/categories");

        try {
            List<String> categories = productService.getAllCategories();
            log.info("Found {} unique categories", categories.size());
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            log.error("Error getting categories: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Product>> getPopularProducts(
            @RequestParam(defaultValue = "8") int limit) {
        log.info("GET /api/products/popular?limit={}", limit);

        try {
            List<Product> products = productService.getPopularProducts(limit);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error getting popular products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/new")
    public ResponseEntity<List<Product>> getNewProducts(
            @RequestParam(defaultValue = "8") int limit) {
        log.info("GET /api/products/new?limit={}", limit);

        try {
            List<Product> products = productService.getNewProducts(limit);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error getting new products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ SZYBKIE WYSZUKIWANIE (autocomplete) - POPRAWIONE
    @GetMapping("/search/quick")
    public ResponseEntity<List<QuickSearchResultDTO>> quickSearch(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int limit) {

        log.info("GET /api/products/search/quick?q={}&limit={}", q, limit);

        try {
            List<Product> products = productService.searchProducts(q)
                    .stream()
                    .limit(limit)
                    .toList();

            List<QuickSearchResultDTO> result = products.stream()
                    .map(p -> QuickSearchResultDTO.builder()
                            .id(p.getProductId())
                            .name(p.getProductName())
                            .price(p.getPrice())
                            .imageUrl(p.getImageUrl() != null ? p.getImageUrl() : "")
                            .category(p.getCategory() != null ? p.getCategory() : "")
                            .build())
                    .toList();

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error in quick search: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ STATYSTYKI PRODUKTÓW - POPRAWIONE (bez Map.of())
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getProductStats() {
        log.info("GET /api/products/stats");

        try {
            List<Product> allProducts = productService.getAllProducts();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalProducts", allProducts.size());
            stats.put("totalCategories", productService.getAllCategories().size());

            if (!allProducts.isEmpty()) {
                Map<String, Integer> priceRange = new HashMap<>();
                priceRange.put("min", allProducts.stream()
                        .mapToInt(Product::getPrice)
                        .min().orElse(0));
                priceRange.put("max", allProducts.stream()
                        .mapToInt(Product::getPrice)
                        .max().orElse(0));
                stats.put("priceRange", priceRange);

                stats.put("averagePrice", allProducts.stream()
                        .mapToInt(Product::getPrice)
                        .average().orElse(0));
            } else {
                Map<String, Integer> priceRange = new HashMap<>();
                priceRange.put("min", 0);
                priceRange.put("max", 0);
                stats.put("priceRange", priceRange);
                stats.put("averagePrice", 0);
            }

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting product stats: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== DTO CLASSES ==========

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchRequest {
        private String query;
    }
}
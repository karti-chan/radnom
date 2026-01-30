package com.example.radnom.controller;

import com.example.radnom.entity.dto.PriceFilterDTO;
import com.example.radnom.entity.Product;
import com.example.radnom.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.origin:http://localhost:5173}")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        log.info("GET /api/products - returning all products");
        try {
            List<Product> products = productService.getAllProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error getting products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
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

    // ✅ NOWA WERSJA - z DTO przez @RequestBody
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

    // ✅ Zachowaj starą wersję dla kompatybilności (opcjonalnie)
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
}

// ✅ USUŃ tę metodę z kontrolera - niepotrzebna!
// public void setProductId(Integer productId) {
//     this.id = productId;
// }
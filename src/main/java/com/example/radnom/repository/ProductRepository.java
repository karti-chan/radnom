package com.example.radnom.repository;

import com.example.radnom.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    // ========== SPRING DATA JPA METHOD NAMES ==========

    // ✅ Filtrowanie po kategorii
    List<Product> findByCategory(String category);

    // ✅ Filtrowanie po zakresie cen
    List<Product> findByPriceBetween(Integer minPrice, Integer maxPrice);

    // ✅ Wyszukiwanie po nazwie (case-insensitive)
    List<Product> findByProductNameContainingIgnoreCase(String name);

    // ✅ Filtrowanie po marce
    List<Product> findByBrand(String brand);

    // ✅ Pobieranie po ID (już masz z JpaRepository, ale możesz nadpisać)
    @Override
    Optional<Product> findById(Integer id);

    // ✅ Sortowanie po cenie rosnąco
    List<Product> findByOrderByPriceAsc();

    // ✅ Sortowanie po cenie malejąco
    List<Product> findByOrderByPriceDesc();

    // ✅ Filtrowanie po dostępności
    List<Product> findByStockGreaterThan(Integer stock);

    // ========== CUSTOM JPQL QUERIES ==========

    // ✅ WYSZUKIWANIE PO NAZWIE (case-insensitive) - JPQL
    @Query("SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchByName(@Param("query") String query);

    // ✅ WYSZUKIWANIE PO NAZWIE LUB OPISIE - JPQL
    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.productName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchByNameOrDescription(@Param("query") String query);

    // ✅ WYSZUKIWANIE PO KATEGORII - JPQL
    @Query("SELECT p FROM Product p WHERE LOWER(p.category) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchByCategory(@Param("query") String query);

    // ✅ WYSZUKIWANIE PO NAZWIE, OPISIE LUB KATEGORII - JPQL
    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.productName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.category) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchByNameOrDescriptionOrCategory(@Param("query") String query);

    // ✅ WYSZUKIWANIE Z SORTOWANIEM - JPQL
    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.productName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "ORDER BY p.price ASC")
    List<Product> searchByNameOrderByPriceAsc(@Param("query") String query);

    // ✅ WYSZUKIWANIE PRODUKTÓW W OKREŚLONYM ZAKRESIE CEN - JPQL
    @Query("SELECT p FROM Product p WHERE " +
            "(LOWER(p.productName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> searchByNameWithPriceRange(
            @Param("query") String query,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice);

    // ✅ POBRANIE WSZYSTKICH UNIKALNYCH KATEGORII
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL")
    List<String> findAllDistinctCategories();

    // ✅ STATYSTYKI PRODUKTÓW
    @Query("SELECT COUNT(p), AVG(p.price), MIN(p.price), MAX(p.price) FROM Product p")
    Object[] getProductStatistics();

    // ✅ WYSZUKIWANIE Z PAGINACJĄ (możesz dodać Pageable)
    // @Query("SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :query, '%'))")
    // Page<Product> searchByNamePaginated(@Param("query") String query, Pageable pageable);
}
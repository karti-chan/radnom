// ProductRepository.java - dodaj te metody:
package com.example.radnom.repository;

import com.example.radnom.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    // Jeśli nie masz - dodaj:
    List<Product> findByCategory(String category);
    // Opcjonalnie - więcej metod filtrowania:
    List<Product> findByPriceBetween(Integer minPrice, Integer maxPrice);
    List<Product> findByProductNameContainingIgnoreCase(String name);
    List<Product> findByBrand(String brand);
    Optional<Product> findById(Integer id);


}
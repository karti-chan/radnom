package com.example.radnom.repository;

import com.example.radnom.entity.Cart;
import com.example.radnom.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // ========== SPRING DATA JPA METHOD NAMES ==========

    // ✅ Szukanie po użytkowniku (encja)
    Optional<Cart> findByUser(User user);

    // ✅ Szukanie po ID użytkownika
    Optional<Cart> findByUserId(Long userId);

    // ========== CUSTOM JPQL QUERIES ==========

    // ✅ Szukanie po email użytkownika (najbardziej użyteczne)
    @Query("SELECT c FROM Cart c WHERE c.user.email = :email")
    Optional<Cart> findByUserEmail(@Param("email") String email);

    // ✅ Szukanie po username użytkownika
    @Query("SELECT c FROM Cart c WHERE c.user.username = :username")
    Optional<Cart> findByUsername(@Param("username") String username);

    // ✅ Sprawdzenie czy użytkownik ma koszyk
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM Cart c WHERE c.user.email = :email")
    boolean existsByUserEmail(@Param("email") String email);
}
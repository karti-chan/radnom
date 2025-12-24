package com.example.radnom.repository;

import com.example.radnom.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // ✅ Znajdź elementy po ID koszyka
    List<CartItem> findByCartId(Long cartId);

    // ✅ Znajdź konkretny element po ID koszyka i ID produktu
    Optional<CartItem> findByCartIdAndProductProductId(Long cartId, Integer productId);

    // ✅ Usuń wszystkie elementy danego koszyka
    void deleteByCartId(Long cartId);

    // ✅ Policz elementy w koszyku - DODAJ METODĘ
    int countByCartId(Long cartId);

    // Alternatywnie możesz użyć @Query:
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :cartId")
    int countItemsByCartId(@Param("cartId") Long cartId);
}
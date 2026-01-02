import React, { createContext, useState, useContext, useEffect } from 'react';
import axios from 'axios';

const CartContext = createContext();

export const useCart = () => useContext(CartContext);

export const CartProvider = ({ children }) => {
    const [cart, setCart] = useState(null);
    const [loading, setLoading] = useState(true);
    const [cartCount, setCartCount] = useState(0); // LICZNIK

    // Funkcja do pobierania nagłówków z tokenem
    const getAuthHeaders = () => {
        const token = localStorage.getItem('token');
        return token ? { Authorization: `Bearer ${token}` } : {};
    };

    // Pobierz cały koszyk
    const fetchCart = async () => {
        try {
            const token = localStorage.getItem('token');
            if (!token) {
                setCart(null);
                setCartCount(0);
                return;
            }

            const response = await axios.get('http://localhost:8080/api/cart', {
                headers: getAuthHeaders()
            });
            setCart(response.data);

            // Oblicz całkowitą liczbę produktów
            if (response.data && response.data.items) {
                const totalItems = response.data.items.reduce(
                    (sum, item) => sum + (item.quantity || 1), 0
                );
                setCartCount(totalItems);
            } else {
                setCartCount(0);
            }
        } catch (error) {
            console.error('Error fetching cart:', error);
            setCart(null);
            setCartCount(0);
        } finally {
            setLoading(false);
        }
    };

    // Pobierz tylko liczbę produktów
    const fetchCartCount = async () => {
        try {
            const token = localStorage.getItem('token');
            if (!token) {
                setCartCount(0);
                return;
            }

            // Użyj endpointu /count
            const response = await axios.get('http://localhost:8080/api/cart/count', {
                headers: getAuthHeaders()
            });
            setCartCount(response.data);
        } catch (error) {
            console.error('Error fetching cart count:', error);
            setCartCount(0);
        }
    };

    // Dodaj do koszyka
    const addToCart = async (productId, quantity = 1) => {
        try {
            const response = await axios.post('http://localhost:8080/api/cart/add',
                null,
                {
                    params: { productId, quantity },
                    headers: getAuthHeaders()
                }
            );
            setCart(response.data);
            await fetchCartCount(); // Odśwież licznik
            return true;
        } catch (error) {
            console.error('Error adding to cart:', error);
            return false;
        }
    };

    // Usuń z koszyka
    const removeFromCart = async (productId) => {
        try {
            const response = await axios.delete(`http://localhost:8080/api/cart/remove/${productId}`, {
                headers: getAuthHeaders()
            });
            setCart(response.data);
            await fetchCartCount();
            return true;
        } catch (error) {
            console.error('Error removing from cart:', error);
            return false;
        }
    };

    // Zaktualizuj ilość
    const updateQuantity = async (productId, quantity) => {
        try {
            const response = await axios.put('http://localhost:8080/api/cart/update',
                null,
                {
                    params: { productId, quantity },
                    headers: getAuthHeaders()
                }
            );
            setCart(response.data);
            await fetchCartCount();
            return true;
        } catch (error) {
            console.error('Error updating cart:', error);
            return false;
        }
    };

    // Wyczyść koszyk
    const clearCart = async () => {
        try {
            await axios.delete('http://localhost:8080/api/cart/clear', {
                headers: getAuthHeaders()
            });
            setCart({ items: [] });
            setCartCount(0);
            return true;
        } catch (error) {
            console.error('Error clearing cart:', error);
            return false;
        }
    };

    // Inicjalizacja
    useEffect(() => {
        fetchCart();

        // Automatyczne odświeżanie co 10 sekund
        const interval = setInterval(fetchCartCount, 10000);
        return () => clearInterval(interval);
    }, []);

    const value = {
        cart,
        cartCount, // DODANE - dostępne w całej aplikacji
        loading,
        addToCart,
        removeFromCart,
        updateQuantity,
        clearCart,
        refreshCart: fetchCart,
        fetchCartCount
    };

    return (
        <CartContext.Provider value={value}>
            {children}
        </CartContext.Provider>
    );
};
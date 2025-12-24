import React, { createContext, useState, useContext, useEffect } from 'react';
import axios from 'axios';

const CartContext = createContext();

export const useCart = () => useContext(CartContext);

export const CartProvider = ({ children }) => {
    const [cart, setCart] = useState(null);
    const [loading, setLoading] = useState(true);

    const fetchCart = async () => {
        try {
            const token = localStorage.getItem('token');
            if (!token) {
                setCart(null);
                return;
            }

            const response = await axios.get('http://localhost:8080/api/cart', {
                headers: { Authorization: `Bearer ${token}` }
            });
            setCart(response.data);
        } catch (error) {
            console.error('Error fetching cart:', error);
            setCart(null);
        } finally {
            setLoading(false);
        }
    };

    const addToCart = async (productId, quantity = 1) => {
        try {
            const token = localStorage.getItem('token');
            const response = await axios.post('http://localhost:8080/api/cart/add',
                { productId, quantity },
                { headers: { Authorization: `Bearer ${token}` } }
            );
            setCart(response.data);
            return true;
        } catch (error) {
            console.error('Error adding to cart:', error);
            return false;
        }
    };

    const removeFromCart = async (productId) => {
        try {
            const token = localStorage.getItem('token');
            const response = await axios.delete(`http://localhost:8080/api/cart/remove/${productId}`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            setCart(response.data);
            return true;
        } catch (error) {
            console.error('Error removing from cart:', error);
            return false;
        }
    };

    const updateQuantity = async (productId, quantity) => {
        try {
            const token = localStorage.getItem('token');
            const response = await axios.put('http://localhost:8080/api/cart/update',
                { productId, quantity },
                { headers: { Authorization: `Bearer ${token}` } }
            );
            setCart(response.data);
            return true;
        } catch (error) {
            console.error('Error updating cart:', error);
            return false;
        }
    };

    const clearCart = async () => {
        try {
            const token = localStorage.getItem('token');
            await axios.delete('http://localhost:8080/api/cart/clear', {
                headers: { Authorization: `Bearer ${token}` }
            });
            setCart({ items: [] });
            return true;
        } catch (error) {
            console.error('Error clearing cart:', error);
            return false;
        }
    };

    useEffect(() => {
        fetchCart();
    }, []);

    const value = {
        cart,
        loading,
        addToCart,
        removeFromCart,
        updateQuantity,
        clearCart,
        refreshCart: fetchCart
    };

    return (
        <CartContext.Provider value={value}>
            {children}
        </CartContext.Provider>
    );
};
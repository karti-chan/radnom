// Cart.jsx
import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';
import './Cart.css';

const Cart = () => {
    const { user } = useAuth();
    const [cart, setCart] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (user) {
            fetchCart();
        }
    }, [user]);

    const fetchCart = async () => {
        try {
            setLoading(true);
            const token = localStorage.getItem('token');
            const response = await axios.get('/api/cart', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            setCart(response.data);
            setError(null);
        } catch (err) {
            setError('Nie udało się załadować koszyka');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const addToCart = async (productId, quantity = 1) => {
        try {
            const token = localStorage.getItem('token');
            const response = await axios.post(`/api/cart/add?productId=${productId}&quantity=${quantity}`, {}, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            setCart(response.data);
        } catch (err) {
            console.error('Błąd dodawania do koszyka:', err);
        }
    };

    const removeFromCart = async (productId) => {
        try {
            const token = localStorage.getItem('token');
            const response = await axios.delete(`/api/cart/remove/${productId}`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            setCart(response.data);
        } catch (err) {
            console.error('Błąd usuwania z koszyka:', err);
        }
    };

    const updateQuantity = async (productId, quantity) => {
        try {
            const token = localStorage.getItem('token');
            const response = await axios.put(`/api/cart/update?productId=${productId}&quantity=${quantity}`, {}, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            setCart(response.data);
        } catch (err) {
            console.error('Błąd aktualizacji ilości:', err);
        }
    };

    const clearCart = async () => {
        try {
            const token = localStorage.getItem('token');
            await axios.delete('/api/cart/clear', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            setCart(null);
        } catch (err) {
            console.error('Błąd czyszczenia koszyka:', err);
        }
    };

    if (loading) return <div className="loading">Ładowanie koszyka...</div>;
    if (error) return <div className="error">{error}</div>;
    if (!cart || cart.items.length === 0) return <div className="empty-cart">Koszyk jest pusty</div>;

    return (
        <div className="cart-container">
            <h2>Twój koszyk</h2>
            
            <div className="cart-items">
                {cart.items.map((item) => (
                    <div key={item.id} className="cart-item">
                        <div className="item-image">
                            {item.imageUrl ? (
                                <img src={item.imageUrl} alt={item.productName} />
                            ) : (
                                <div className="no-image">Brak zdjęcia</div>
                            )}
                        </div>
                        
                        <div className="item-details">
                            <h3>{item.productName}</h3>
                            <p>Cena: {item.formattedPrice}</p>
                            
                            <div className="quantity-controls">
                                <button 
                                    onClick={() => updateQuantity(item.productId, item.quantity - 1)}
                                    disabled={item.quantity <= 1}
                                >
                                    -
                                </button>
                                <span>{item.quantity}</span>
                                <button onClick={() => updateQuantity(item.productId, item.quantity + 1)}>
                                    +
                                </button>
                            </div>
                        </div>
                        
                        <div className="item-total">
                            <p>Suma: {item.formattedTotalPrice}</p>
                            <button 
                                onClick={() => removeFromCart(item.productId)}
                                className="remove-btn"
                            >
                                Usuń
                            </button>
                        </div>
                    </div>
                ))}
            </div>
            
            <div className="cart-summary">
                <div className="summary-details">
                    <h3>Podsumowanie</h3>
                    <p>Liczba produktów: {cart.totalItems}</p>
                    <p className="total-price">Łączna kwota: {cart.formattedTotalPrice}</p>
                </div>
                
                <div className="cart-actions">
                    <button onClick={clearCart} className="clear-btn">
                        Wyczyść koszyk
                    </button>
                    <button className="checkout-btn">
                        Przejdź do kasy
                    </button>
                </div>
            </div>
        </div>
    );
};

export default Cart;
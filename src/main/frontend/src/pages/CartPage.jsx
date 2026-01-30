import React, { useState, useEffect } from 'react';
import { useCart } from '../context/CartContext';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const CartPage = () => {
  const { cart, loading, updateQuantity, removeFromCart, clearCart, refreshCart } = useCart();
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [updating, setUpdating] = useState({});

  // Ładuj koszyk tylko raz przy wejściu na stronę
  useEffect(() => {
      console.log('🛒 CartPage - auth changed:', isAuthenticated);

      if (isAuthenticated) {
        console.log('✅ User authenticated, loading cart...');
        refreshCart();
      } else {
        console.log('❌ User not authenticated, redirecting...');
        navigate('/');
      }
    }, [isAuthenticated, refreshCart, navigate]); // ⬅️ Reaguj na zmiany!

  // Debugowanie
  useEffect(() => {
    console.log('📊 CartPage - cart updated:', cart);
    console.log('- items count:', cart?.items?.length);
    console.log('- cart object:', cart);
  }, [cart]);

  // Jeśli niezalogowany, pokaż komunikat
  if (!isAuthenticated) {
    return (
      <div className="empty-cart">
        <h2>🔐 Wymagane logowanie</h2>
        <p>Aby zobaczyć koszyk, musisz się zalogować.</p>
        <button
          onClick={() => navigate('/')}
          className="btn btn-primary"
        >
          ← Wróć do sklepu
        </button>
      </div>
    );
  }

  if (loading) {
    return <div className="loading">Ładowanie koszyka...</div>;
  }

  if (!cart || !cart.items || cart.items.length === 0) {
    return (
      <div className="empty-cart">
        <h2>🛒 Twój koszyk jest pusty</h2>
        <p>Dodaj produkty, aby zobaczyć je tutaj</p>
        <Link to="/" className="btn btn-primary">
          ← Przejdź do sklepu
        </Link>
      </div>
    );
  }

  const handleQuantityChange = async (productId, newQuantity) => {
    if (newQuantity < 1) {
      if (window.confirm('Czy chcesz usunąć produkt z koszyka?')) {
        await removeFromCart(productId);
      }
      return;
    }

    setUpdating({ ...updating, [productId]: true });
    try {
      await updateQuantity(productId, newQuantity);
    } finally {
      setUpdating({ ...updating, [productId]: false });
    }
  };

  const calculateTotal = () => {
    return cart.items.reduce((total, item) => {
      return total + (item.price * item.quantity);
    }, 0).toFixed(2);
  };

  return (
    <div className="cart-page container">
      <h1>🛒 Twój koszyk</h1>

      <div className="cart-content">
        <div className="cart-items">
          {cart.items.map((item) => (
            <div key={item.id || item.productId} className="cart-item">
              <div className="item-image">
                {item.imageUrl ? (
                  <img src={item.imageUrl} alt={item.productName} />
                ) : (
                  <div className="image-placeholder">🛒</div>
                )}
              </div>

              <div className="item-details">
                <h3 className="item-name">{item.productName}</h3>
                <p className="item-price">{item.price} zł</p>

                <div className="item-quantity">
                  <button
                    className="quantity-btn"
                    onClick={() => handleQuantityChange(item.productId, item.quantity - 1)}
                    disabled={updating[item.productId]}
                  >
                    −
                  </button>

                  <span className="quantity-value">
                    {updating[item.productId] ? '...' : item.quantity}
                  </span>

                  <button
                    className="quantity-btn"
                    onClick={() => handleQuantityChange(item.productId, item.quantity + 1)}
                    disabled={updating[item.productId]}
                  >
                    +
                  </button>
                </div>

                <div className="item-subtotal">
                  Suma: <strong>{(item.price * item.quantity).toFixed(2)} zł</strong>
                </div>
              </div>

              <button
                className="remove-btn"
                onClick={() => removeFromCart(item.productId)}
                title="Usuń z koszyka"
              >
                ❌
              </button>
            </div>
          ))}
        </div>

        <div className="cart-summary">
          <h2>Podsumowanie</h2>

          <div className="summary-row">
            <span>Liczba produktów:</span>
            <span>{cart.items.length}</span>
          </div>

          <div className="summary-row">
            <span>Razem:</span>
            <span className="total-price">{calculateTotal()} zł</span>
          </div>

          <div className="summary-actions">
            <button
              className="btn btn-danger"
              onClick={clearCart}
              disabled={cart.items.length === 0}
            >
              🗑️ Wyczyść koszyk
            </button>

            <Link to="/checkout" className="btn btn-success">
              💳 Przejdź do kasy
            </Link>
          </div>

          <div className="continue-shopping">
            <Link to="/">← Kontynuuj zakupy</Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CartPage;
import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import SearchBar from './SearchBar'; // ✅ DODAJ IMPORT
import AuthModal from './auth/AuthModal';
import './Navbar.css';

const Navbar = () => {
  const { user, logout, isAuthenticated } = useAuth();
  const { cartCount } = useCart();
  const [showAuthModal, setShowAuthModal] = useState(false);

  // Debug
  useEffect(() => {
    console.log('🎯 Navbar - cartCount updated:', cartCount);
    console.log('🎯 Navbar - isAuthenticated:', isAuthenticated);
  }, [cartCount, isAuthenticated]);

  return (
    <nav className="navbar">
      {/* ✅ LEWA CZĘŚĆ - LOGO/SKLEP */}
      <div className="nav-brand">
        <Link to="/" style={{ textDecoration: 'none', color: 'inherit' }}>
          Sklep
        </Link>
      </div>

      {/* ✅ ŚRODKOWA CZĘŚĆ - WYSZUKIWARKA */}
      <div className="navbar-center">
        <SearchBar /> {/* ✅ WYSZUKIWARKA DODANA */}
      </div>

      {/* ✅ PRAWA CZĘŚĆ - KOSZYK, LOGOWANIE */}
      <div className="nav-items">
        {isAuthenticated && (
          <Link to="/cart" className="cart-link">
            <div className="cart-icon-container">
              🛒
              <span
                className="cart-badge"
                style={{
                  opacity: cartCount > 0 ? 1 : 0.3,
                  backgroundColor: cartCount > 0 ? '#ff4444' : '#999'
                }}
              >
                {cartCount}
              </span>
            </div>
          </Link>
        )}

        {isAuthenticated ? (
          <>
            <span className="user-greeting">
              Witaj, {user?.username}!
            </span>
            <button onClick={logout} className="logout-button">
              Wyloguj
            </button>
          </>
        ) : (
          <button
            onClick={() => setShowAuthModal(true)}
            className="login-button"
          >
            Zaloguj się
          </button>
        )}
      </div>

      <AuthModal
        isOpen={showAuthModal}
        onClose={() => setShowAuthModal(false)}
      />
    </nav>
  );
};

export default Navbar;
// src/main/frontend/src/components/Navbar.jsx
import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import AuthModal from './auth/AuthModal';

const Navbar = () => {
  const { user, logout, isAuthenticated } = useAuth();
  const [showAuthModal, setShowAuthModal] = useState(false);

  return (
    <nav className="navbar">
      <div className="nav-brand">Sklep</div>
      
      <div className="nav-items">
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
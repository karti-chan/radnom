// src/main/frontend/src/context/AuthContext.jsx
import React, { createContext, useState, useContext, useEffect } from 'react';

// 1. DODAJ DOMYŚLNĄ WARTOŚĆ z funkcjami które nie crashują
const AuthContext = createContext({
  user: null,
  login: async () => ({ success: false, error: 'AuthContext not initialized' }),
  register: async () => ({ success: false, error: 'AuthContext not initialized' }),
  logout: () => console.warn('AuthContext not initialized'),
  loading: true,
  isAuthenticated: false
});

// 2. POPRAW useAuth z lepszym error handlingiem
export const useAuth = () => {
  const context = useContext(AuthContext);
  
  // DODAJ LOG dla debugowania
  console.log('🔍 useAuth called, context:', context);
  
  // Jeśli context jest undefined (nie w AuthProvider), zwróć bezpieczną wartość
  if (context === undefined) {
    console.error('❌ useAuth() used outside AuthProvider!');
    return {
      user: null,
      login: async () => ({ success: false, error: 'Not in AuthProvider' }),
      register: async () => ({ success: false, error: 'Not in AuthProvider' }),
      logout: () => {},
      loading: false,
      isAuthenticated: false
    };
  }
  
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // DODAJ LOG
  console.log('🔄 AuthProvider rendering, user:', user);

  useEffect(() => {
    console.log('📦 AuthProvider useEffect - checking localStorage');
    const token = localStorage.getItem('token');
    const userData = localStorage.getItem('user');
    
    console.log('📦 Token from localStorage:', token);
    console.log('📦 User data from localStorage:', userData);
    
    if (token && userData) {
      try {
        const parsedUser = JSON.parse(userData);
        console.log('✅ Parsed user:', parsedUser);
        setUser(parsedUser);
      } catch (error) {
        console.error('❌ Error parsing user data:', error);
        localStorage.removeItem('user'); // wyczyść nieprawidłowe dane
      }
    }
    setLoading(false);
  }, []);

  const login = async (username, password) => {
    console.log('🔐 login() called with username:', username);
    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
      });

      console.log('📡 Login response status:', response.status);
      
      if (!response.ok) {
        const errorText = await response.text();
        console.error('❌ Login failed:', errorText);
        throw new Error(errorText || 'Błąd logowania');
      }

      const data = await response.json();
      console.log('✅ Login success, data:', data);
      
      localStorage.setItem('token', data.token);
      localStorage.setItem('user', JSON.stringify(data.user || { username }));
      setUser(data.user || { username });
      return { success: true };
    } catch (error) {
      console.error('❌ Login error:', error);
      return { success: false, error: error.message };
    }
  };

  const register = async (username, email, password) => {
    console.log('📝 register() called:', { username, email });
    try {
      const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, email, password }),
      });

      console.log('📡 Register response status:', response.status);
      
      if (!response.ok) {
        const errorText = await response.text();
        console.error('❌ Register failed:', errorText);
        throw new Error(errorText || 'Błąd rejestracji');
      }

      const text = await response.text();
      console.log('✅ Register success:', text);
      return { success: true };
    } catch (error) {
      console.error('❌ Register error:', error);
      return { success: false, error: error.message };
    }
  };

  const logout = () => {
    console.log('🚪 logout() called');
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  const value = {
    user,
    login,
    register,
    logout,
    loading,
    isAuthenticated: !!user,
  };

  console.log('🎯 AuthProvider value:', value);

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
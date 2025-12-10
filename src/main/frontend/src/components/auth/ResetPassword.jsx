import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

const ResetPassword = () => {
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [token, setToken] = useState('');
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    // Pobierz token z URL query params
    const params = new URLSearchParams(location.search);
    const urlToken = params.get('token');
    
    if (urlToken) {
      setToken(urlToken);
    } else {
      setError('Brak tokenu resetu. Użyj linku z emaila.');
    }
  }, [location]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');

    if (newPassword !== confirmPassword) {
      setError('Hasła nie są identyczne');
      return;
    }

    if (newPassword.length < 6) {
      setError('Hasło musi mieć co najmniej 6 znaków');
      return;
    }

    setLoading(true);

    try {
      const response = await fetch('/api/auth/reset-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
          token, 
          newPassword 
        }),
      });

      const text = await response.text();
      
      if (response.ok) {
        setMessage('Hasło zostało pomyślnie zmienione! Możesz się teraz zalogować.');
        setTimeout(() => navigate('/login'), 3000);
      } else {
        setError(text || 'Wystąpił błąd resetowania hasła');
      }
    } catch (err) {
      setError('Problem z połączeniem: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-form-container">
      <h2>Resetowanie hasła</h2>
      
      <form onSubmit={handleSubmit} className="auth-form">
        <div className="form-group">
          <label>Nowe hasło:</label>
          <input
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            required
            placeholder="Minimum 6 znaków"
          />
        </div>
        
        <div className="form-group">
          <label>Potwierdź nowe hasło:</label>
          <input
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
            placeholder="Powtórz hasło"
          />
        </div>
        
        {message && <div className="success-message">{message}</div>}
        {error && <div className="error-message">{error}</div>}
        
        <button 
          type="submit" 
          disabled={loading || !token} 
          className="auth-button"
        >
          {loading ? 'Zmienianie...' : 'Zmień hasło'}
        </button>
        
        <div className="auth-links">
          <button 
            type="button" 
            className="link-button"
            onClick={() => navigate('/login')}
          >
            Wróć do logowania
          </button>
        </div>
      </form>
    </div>
  );
};

export default ResetPassword;
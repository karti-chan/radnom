import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const ForgotPassword = () => {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');
    setLoading(true);

    try {
      const response = await fetch('/api/auth/forgot-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email }),
      });

      const text = await response.text();
      
      if (response.ok) {
        setMessage('Instrukcje resetu hasła zostały wysłane na podany adres email (sprawdź konsolę backendu w dev mode).');
        setTimeout(() => navigate('/'), 3000);
      } else {
        setError(text || 'Wystąpił błąd');
      }
    } catch (err) {
      setError('Problem z połączeniem: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-form-container">
      <h2>Zapomniałem hasła</h2>
      <p className="auth-info">
        Podaj adres email powiązany z Twoim kontem, a wyślemy Ci link do resetu hasła.
      </p>
      
      <form onSubmit={handleSubmit} className="auth-form">
        <div className="form-group">
          <label>Email:</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            placeholder="twój@email.pl"
          />
        </div>
        
        {message && <div className="success-message">{message}</div>}
        {error && <div className="error-message">{error}</div>}
        
        <button type="submit" disabled={loading} className="auth-button">
          {loading ? 'Wysyłanie...' : 'Wyślij link resetu'}
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

export default ForgotPassword;
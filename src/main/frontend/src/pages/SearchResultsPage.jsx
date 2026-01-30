// src/pages/SearchResultsPage.jsx
import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import './SearchResultsPage.css';

const SearchResultsPage = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { addToCart } = useCart();
  const { isAuthenticated } = useAuth();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const query = params.get('q') || '';
    setSearchQuery(query);

    if (query) {
      fetchSearchResults(query);
    } else {
      navigate('/');
    }
  }, [location.search]);

  const fetchSearchResults = async (query) => {
    setLoading(true);
    try {
      const response = await axios.get(`http://localhost:8081/api/products/search?q=${encodeURIComponent(query)}`);
      setProducts(response.data);
    } catch (error) {
      console.error('Error fetching search results:', error);
      setProducts([]);
    } finally {
      setLoading(false);
    }
  };

  const handleAddToCart = async (product) => {
    if (!isAuthenticated) {
      alert('Musisz się zalogować, aby dodać produkt do koszyka');
      return;
    }

    try {
      const productId = product.productId || product.id;
      const success = await addToCart(productId, 1);

      if (success) {
        alert(`✅ Dodano "${product.productName}" do koszyka!`);
      }
    } catch (error) {
      console.error('Error adding to cart:', error);
      alert('Nie udało się dodać do koszyka');
    }
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Wyszukiwanie produktów...</p>
      </div>
    );
  }

  return (
    <div className="search-results-container">
      <div className="search-results-header">
        <h1>
          {products.length > 0
            ? `Znaleziono ${products.length} produktów dla "${searchQuery}"`
            : `Brak wyników dla "${searchQuery}"`
          }
        </h1>
        <button onClick={() => navigate(-1)} className="back-button">
          ← Wróć
        </button>
      </div>

      {products.length > 0 ? (
        <div className="products-grid">
          {products.map(product => (
            <div key={product.productId || product.id} className="product-card">
              <Link to={`/product/${product.productId || product.id}`} className="product-link">
                <div className="product-image">
                  {product.imageUrl ? (
                    <img src={product.imageUrl} alt={product.productName} />
                  ) : (
                    <div className="no-image">🛒</div>
                  )}
                </div>
                <div className="product-info">
                  <h3>{product.productName}</h3>
                  <p className="price">{product.price} zł</p>
                  {product.category && <p className="category">{product.category}</p>}
                </div>
              </Link>
              <button
                onClick={() => handleAddToCart(product)}
                className="add-to-cart-btn"
                disabled={!isAuthenticated}
              >
                🛒 Dodaj do koszyka
              </button>
            </div>
          ))}
        </div>
      ) : (
        <div className="no-results-message">
          <p>😔 Nie znaleziono produktów pasujących do wyszukiwania.</p>
          <button onClick={() => navigate('/')} className="btn btn-primary">
            Przejdź do sklepu
          </button>
        </div>
      )}
    </div>
  );
};

export default SearchResultsPage;
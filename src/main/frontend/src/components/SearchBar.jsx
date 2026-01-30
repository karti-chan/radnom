// src/components/SearchBar.jsx
import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './SearchBar.css';

const SearchBar = () => {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showResults, setShowResults] = useState(false);
  const navigate = useNavigate();
  const searchRef = useRef(null);

  // Zamknij dropdown po kliknięciu poza
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (searchRef.current && !searchRef.current.contains(event.target)) {
        setShowResults(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Wyszukiwanie z debounce (opóźnienie 300ms)
  useEffect(() => {
    if (!query.trim()) {
      setResults([]);
      setShowResults(false);
      return;
    }

    const timer = setTimeout(() => {
      performSearch(query);
    }, 300);

    return () => clearTimeout(timer);
  }, [query]);

  const performSearch = async (searchQuery) => {
    if (!searchQuery.trim()) return;

    setLoading(true);
    try {
      const response = await axios.get(`http://localhost:8081/api/products/search`, {
        params: { q: searchQuery }
      });

      setResults(response.data);
      setShowResults(true);
    } catch (error) {
      console.error('Search error:', error);
      setResults([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    if (query.trim()) {
      navigate(`/search?q=${encodeURIComponent(query)}`);
      setShowResults(false);
      setQuery('');
    }
  };

  const handleResultClick = (productId) => {
    navigate(`/product/${productId}`);
    setShowResults(false);
    setQuery('');
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && query.trim()) {
      handleSearch(e);
    }
    if (e.key === 'Escape') {
      setShowResults(false);
    }
  };

  return (
    <div className="search-container" ref={searchRef}>
      <form onSubmit={handleSearch} className="search-form">
        <div className="search-input-wrapper">
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onKeyDown={handleKeyDown}
            onFocus={() => query.trim() && setShowResults(true)}
            placeholder="Szukaj produktów..."
            className="search-input"
            aria-label="Szukaj produktów"
          />
          <button type="submit" className="search-button">
            🔍
          </button>
          {loading && <div className="search-spinner"></div>}
        </div>
      </form>

      {/* Dropdown z wynikami */}
      {showResults && results.length > 0 && (
        <div className="search-results-dropdown">
          <div className="results-header">
            <span>Znalezione produkty ({results.length})</span>
          </div>
          {results.slice(0, 8).map((product) => (
            <div
              key={product.productId || product.id}
              className="search-result-item"
              onClick={() => handleResultClick(product.productId || product.id)}
            >
              <div className="result-image">
                {product.imageUrl ? (
                  <img src={product.imageUrl} alt={product.productName} />
                ) : (
                  <div className="no-image">🛒</div>
                )}
              </div>
              <div className="result-details">
                <div className="result-name">{product.productName}</div>
                <div className="result-price">{product.price} zł</div>
                {product.category && (
                  <div className="result-category">{product.category}</div>
                )}
              </div>
            </div>
          ))}
          {results.length > 8 && (
            <div className="results-footer">
              <button
                onClick={handleSearch}
                className="show-all-button"
              >
                Pokaż wszystkie ({results.length})
              </button>
            </div>
          )}
        </div>
      )}

      {/* Brak wyników */}
      {showResults && query.trim() && !loading && results.length === 0 && (
        <div className="search-results-dropdown">
          <div className="no-results">
            😔 Nie znaleziono produktów dla "{query}"
          </div>
        </div>
      )}
    </div>
  );
};

export default SearchBar;
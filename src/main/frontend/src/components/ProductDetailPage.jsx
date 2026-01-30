// src/pages/ProductDetailPage.jsx
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import './ProductDetailPage.css';

const ProductDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const { isAuthenticated } = useAuth();
  const { addToCart } = useCart();

  useEffect(() => {
    fetchProduct();
  }, [id]);

  const fetchProduct = async () => {
    try {
      setLoading(true);
      console.log(`📦 Fetching product ${id}...`);

      const response = await axios.get(`http://localhost:8081/api/products/${id}`);

      console.log('✅ Product data:', response.data);
      setProduct(response.data);
    } catch (err) {
      console.error('❌ Error fetching product:', err);
      setError('Produkt nie został znaleziony');
    } finally {
      setLoading(false);
    }
  };

  const handleAddToCart = async () => {
    console.log('=== DEBUG ADD TO CART FROM PRODUCT DETAILS ===');
    console.log('id from URL:', id, 'type:', typeof id);

    if (!isAuthenticated) {
      alert('Musisz się zalogować, aby dodać produkt do koszyka');
      return;
    }

    if (!product) {
      console.error('❌ Product data not loaded yet');
      alert('Nie można dodać produktu - dane nie zostały załadowane');
      return;
    }

    try {
      // ✅ Pobierz productId z obiektu produktu
      const productId = product.productId || product.id;

      console.log(`🛒 Adding product from details page:`);
      console.log(`- Product ID: ${productId} (from product object)`);
      console.log(`- Product Name: ${product.productName}`);
      console.log(`- Quantity: ${quantity}`);
      console.log(`- isAuthenticated: ${isAuthenticated}`);

      // ✅ Przekaż productId jako LICZBĘ
      const success = await addToCart(parseInt(productId), quantity);

      if (success) {
        alert(`✅ Dodano ${quantity}x "${product.productName}" do koszyka!`);
      } else {
        alert('❌ Nie udało się dodać do koszyka. Sprawdź konsolę.');
      }
    } catch (error) {
      console.error('❌ Error adding to cart from product details:', error);
      alert('Nie udało się dodać do koszyka: ' + error.message);
    }
  };

  // Debug info
  useEffect(() => {
    console.log('🔍 PRODUCT PAGE DEBUG:');
    console.log('- isAuthenticated:', isAuthenticated);
    console.log('- product:', product);
    console.log('- product?.stock:', product?.stock);
    console.log('- id from URL:', id);
  }, [isAuthenticated, product, id]);

  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Ładowanie produktu...</p>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="error-container">
        <h2>😕 Produkt nie znaleziony</h2>
        <p>Przepraszamy, ten produkt nie istnieje lub został usunięty.</p>
        <button onClick={() => navigate('/')} className="btn btn-primary">
          ← Wróć do sklepu
        </button>
      </div>
    );
  }

  return (
    <div className="product-detail-container">
      <button onClick={() => navigate(-1)} className="back-button">
        ← Wróć
      </button>

      <div className="product-detail">
        <div className="product-image-section">
          <img
            src={product.imageUrl || '/images/default-product.jpg'}
            alt={product.productName}
            className="product-main-image"
            onError={(e) => {
              e.target.src = '/images/default-product.jpg';
            }}
          />
        </div>

        <div className="product-info-section">
          <h1 className="product-title">{product.productName}</h1>

          <div className="product-price-section">
            <span className="product-price">{product.price} zł</span>
            {product.stock > 0 ? (
              <span className="in-stock">✅ Dostępny</span>
            ) : (
              <span className="out-of-stock">❌ Niedostępny</span>
            )}
          </div>

          <div className="product-meta">
            {product.category && (
              <span className="category-badge">{product.category}</span>
            )}
            {product.brand && <span>Marka: <strong>{product.brand}</strong></span>}
          </div>

          <div className="product-description">
            <h3>Opis produktu</h3>
            <p>{product.description || 'Brak opisu produktu.'}</p>
          </div>

          <div className="product-specs">
            {product.weight && <p><strong>Waga:</strong> {product.weight} kg</p>}
            {product.dimensions && <p><strong>Wymiary:</strong> {product.dimensions}</p>}
            {product.stock !== undefined && <p><strong>Dostępna ilość:</strong> {product.stock} szt.</p>}
          </div>

          <div className="add-to-cart-section">
            <div className="quantity-selector">
              <button
                onClick={() => setQuantity(Math.max(1, quantity - 1))}
                className="quantity-btn"
              >
                −
              </button>
              <input
                type="number"
                value={quantity}
                onChange={(e) => setQuantity(Math.max(1, parseInt(e.target.value) || 1))}
                min="1"
                max={product.stock || 99}
                className="quantity-input"
              />
              <button
                onClick={() => setQuantity(quantity + 1)}
                className="quantity-btn"
              >
                +
              </button>
            </div>

            <button
              onClick={handleAddToCart}
              className="add-to-cart-btn"

            >
              🛒 Dodaj do koszyka
            </button>
          </div>

          <div className="product-actions">
            <button className="btn btn-secondary">
              💖 Dodaj do ulubionych
            </button>
            <button className="btn btn-secondary">
              📤 Udostępnij
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductDetailPage;
import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useCart } from '../context/CartContext'

function ProductList() {
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const { addToCart } = useCart() // ⬅️ DODAJ hook z CartContext

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        console.log('🔄 Pobieram produkty z:', 'http://localhost:8081/api/products')

        const response = await fetch('http://localhost:8081/api/products')

        console.log('📡 Status odpowiedzi:', response.status)
        console.log('📡 Czy OK?:', response.ok)

        const data = await response.json()
        console.log('📦 Otrzymane dane:', data)

        setProducts(data)
      } catch (error) {
        console.error('❌ Błąd:', error)
      } finally {
        setLoading(false)
      }
    }

    fetchProducts()
  }, [])

  // ================== NOWA POPRAWIONA FUNKCJA handleAddToCart ==================
  const handleAddToCart = async (product) => {
    try {
        console.log(`🛒 handleAddToCart called for product:`, product);
        console.log(`🛒 Product ID: ${product.productId}, Name: ${product.productName}`);

        // Sprawdź czy użytkownik jest zalogowany
        const token = localStorage.getItem('token');
        console.log('🔑 Token exists?', !!token);
        console.log('🔑 Token (first 20 chars):', token?.substring(0, 20));

        if (!token) {
            alert('Musisz się zalogować, aby dodawać produkty do koszyka!');
            return;
        }

        const success = await addToCart(product.productId, 1);

        if (success) {
            alert('✅ Produkt dodany do koszyka!');
        } else {
            alert('❌ Nie udało się dodać do koszyka. Sprawdź konsolę.');
        }
    } catch (error) {
        console.error('❌ Error in handleAddToCart:', error);
        alert('Wystąpił błąd: ' + error.message);
    }
  };
  // ================== KONIEC NOWEJ FUNKCJI handleAddToCart ==================

  if (loading) {
    return <div className="loading">Ładowanie...</div>
  }

  return (
    <div className="container">
      {/* Filtry */}
      <aside className="filters-sidebar">
        <h2>FILTRY</h2>
        <div className="filter-section">
          <h3>Kategoria</h3>
          <label><input type="checkbox" /> Warzywa</label>
          <label><input type="checkbox" /> Nabiał</label>
          <label><input type="checkbox" /> Mięso</label>
          <label><input type="checkbox" /> Jaja</label>
        </div>

        <div className="filter-section">
          <h3>Cena</h3>
          <div className="price-inputs">
            <input type="number" defaultValue="0" />
            <span>-</span>
            <input type="number" defaultValue="24" />
          </div>
        </div>
      </aside>

      {/* Produkty */}
      <main className="products-main">
        <div className="products-header">
          <div className="results-count">
            Znalezione produkty: <strong>{products.length}</strong>
          </div>
        </div>

        <div className="products-grid">
          {products.map(product => (
            <div key={product.productId} className="product-card">
              <div className="product-image">
                {product.imageUrl ? (
                  <img src={product.imageUrl} alt={product.productName} />
                ) : (
                  <>
                    {product.productName === 'Marchew' && '🥕'}
                    {product.productName === 'Ser' && '🧀'}
                    {product.productName === 'Jajko' && '🥚'}
                    {product.productName === 'Szynka' && '🍖'}
                  </>
                )}
              </div>

              <div className="product-info">
                {/* LINK DO STRONY PRODUKTU */}
                <Link to={`/product/${product.productId}`} className="product-link">
                  <h3 className="product-name">{product.productName}</h3>
                </Link>

                <div className="price-section">
                  <span className="current-price">{product.price} zł</span>
                </div>

                {product.category && (
                  <div className="product-category">Kategoria: {product.category}</div>
                )}

                <div className="product-actions">
                  {/* ⬇️⬇️⬇️ PRZYCISK DODAJ DO KOSZYKA ⬇️⬇️⬇️ */}
                  <button
                    className="add-to-cart-btn"
                    onClick={() => handleAddToCart(product)}  // ⬅️ używa nowej funkcji
                  >
                    🛒 Dodaj do koszyka
                  </button>
                  {/* ⬆️⬆️⬆️ KONIEC PRZYCISKU ⬆️⬆️⬆️ */}
                  
                  {/* LINK DO SZCZEGÓŁÓW */}
                  <Link to={`/product/${product.productId}`} className="details-btn">
                    ℹ️ Szczegóły
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      </main>
    </div>
  )
}

export default ProductList
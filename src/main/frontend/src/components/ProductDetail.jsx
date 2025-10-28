import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'

function ProductDetail() {
  const { id } = useParams()
  const [product, setProduct] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        console.log('🔄 Szukam produktu ID:', id)
        const response = await fetch(`http://localhost:8080/api/products/${id}`)
        
        if (!response.ok) {
          throw new Error('Product not found')
        }
        
        const data = await response.json()
        console.log('📦 Znaleziony produkt:', data)
        setProduct(data)
      } catch (error) {
        console.error('❌ Błąd:', error)
        setError(error.message)
      } finally {
        setLoading(false)
      }
    }

    fetchProduct()
  }, [id])

  if (loading) return <div>Ładowanie...</div>
  if (error) return <div>Błąd: {error}</div>
  if (!product) return <div>Produkt nie znaleziony</div>

  return (
    <div className="product-detail">
      <nav>
        <Link to="/">🏠 Strona główna</Link> / {product.productName}
      </nav>
      
      <h1>{product.productName}</h1>
      <p>Cena: {product.price} PLN</p>
      <p>Kategoria: {product.category}</p>
      <p>Opis: {product.description}</p>
      
      <Link to="/">← Wróć do listy</Link>
    </div>
  )
}

export default ProductDetail
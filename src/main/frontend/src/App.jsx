import { Routes, Route, Link } from 'react-router-dom' 

import ProductList from './components/ProductList'
import ProductDetail from './components/ProductDetail'
import Cart from './components/Cart'
import './App.css'

function App() {
  return (
    // ← USUŃ <Router> 
    <div className="App">
      <nav className="app-nav">
        <Link to="/">🏪 Sklep</Link>
        <Link to="/cart">🛒 Koszyk</Link>  
      </nav>

      <Routes>
        <Route path="/" element={<ProductList />} />
        <Route path="/product/:id" element={<ProductDetail />} />
        <Route path="/cart" element={<Cart />} />
      </Routes>
    </div>
    // ← USUŃ </Router>
  )
}

export default App
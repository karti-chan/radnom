import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
  port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',  // ← SPRING BOOT BACKEND
        changeOrigin: true,
        secure: false,
        // rewrite: (path) => path.replace(/^\/api/, '') // opcjonalnie
      }
    }
  },
  build: {
    outDir: '../../resources/static',
    emptyOutDir: true,
    sourcemap: true,
  }
})
// src/axiosSetup.js
import axios from 'axios';

// Konfiguracja globalna axios
axios.defaults.baseURL = 'http://localhost:8081';
axios.defaults.headers.common['Content-Type'] = 'application/json';

// Interceptor request - automatycznie dodaje token
axios.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token');
        if (token && token.startsWith('eyJ')) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        console.log(`📤 ${config.method?.toUpperCase()} ${config.url}`);
        return config;
    },
    error => {
        console.error('❌ Request error:', error);
        return Promise.reject(error);
    }
);

// Interceptor response - obsługa błędów
axios.interceptors.response.use(
    response => {
        console.log(`📥 ${response.status} ${response.config.url}`);
        return response;
    },
    error => {
        if (error.response?.status === 401) {
            console.error('❌ 401 Unauthorized - clearing token');
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '/'; // przekieruj do logowania
        }
        return Promise.reject(error);
    }
);

export default axios;
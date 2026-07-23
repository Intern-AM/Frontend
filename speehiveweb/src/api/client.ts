import axios from 'axios';

export const SERVER_ORIGIN = 'https://debian.tailbd6bc8.ts.net:8443/';

// In Vite dev mode, route via proxy /api, otherwise use SERVER_ORIGIN
export const BASE_URL = import.meta.env.DEV ? '' : SERVER_ORIGIN;

export const apiClient = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000,
});

// Request Interceptor: Attach JWT Token if present
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('hive_auth_token');
    if (token && config.headers && !config.headers.Authorization) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Graceful Error Handling across Network, 401, 403, and 500 errors
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
      console.warn('Backend API request timed out. Operating in offline graceful mode.');
    } else if (error.response) {
      const status = error.response.status;
      if (status === 401) {
        console.warn('Session token expired or unauthorized access.');
      } else if (status === 403) {
        console.warn('Forbidden access: Insufficient role permissions for this operation.');
      } else if (status >= 500) {
        console.warn('Backend server internal error (5xx). Operating with local state fallbacks.');
      }
    } else if (error.request) {
      console.warn('Network error: Failed to connect to backend server at ' + SERVER_ORIGIN);
    }
    return Promise.reject(error);
  }
);

// Helper to format image URLs gracefully
export const getFormattedImageUrl = (url: string | null | undefined): string => {
  if (!url || typeof url !== 'string' || url.trim().length === 0) return '';
  
  const trimmed = url.trim();
  
  if (trimmed.startsWith('http://') || trimmed.startsWith('https://')) {
    if (
      trimmed.includes('172.16.70.37') ||
      trimmed.includes('172.16.50.91') ||
      trimmed.includes('localhost') ||
      trimmed.includes('127.0.0.1') ||
      trimmed.includes('10.0.2.2')
    ) {
      try {
        const urlObj = new URL(trimmed);
        const path = urlObj.pathname.startsWith('/') ? urlObj.pathname.slice(1) : urlObj.pathname;
        return `${SERVER_ORIGIN}${path}${urlObj.search}`;
      } catch (e) {
        return trimmed;
      }
    }
    return trimmed;
  }
  
  const cleanPath = trimmed.startsWith('/') ? trimmed.slice(1) : trimmed;
  return `${SERVER_ORIGIN}${cleanPath}`;
};

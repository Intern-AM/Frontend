import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
  ],
  server: {
    port: 5173,
    host: true,
    proxy: {
      '/api': {
        target: 'https://debian.tailbd6bc8.ts.net',
        changeOrigin: true,
        secure: false, // Allows bypass of SSL/TLS certificate constraints during dev
      }
    }
  }
});

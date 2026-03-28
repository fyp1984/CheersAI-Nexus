import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api/v1/auth': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/v1/users': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/api/v1/feedbacks': {
        target: 'http://localhost:8083',
        changeOrigin: true
      },
      '/api/v1/products': {
        target: 'http://localhost:8084',
        changeOrigin: true
      },
      '/api/v1/plans': {
        target: 'http://localhost:8085',
        changeOrigin: true
      },
      '/api/v1/subscriptions': {
        target: 'http://localhost:8085',
        changeOrigin: true
      },
      '/api/v1/audit': {
        target: 'http://localhost:8086',
        changeOrigin: true
      }
    }
  }
})

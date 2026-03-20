import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api/v1/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/api/v1/users': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/v1/feedbacks': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})

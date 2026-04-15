import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, new URL('.', import.meta.url).pathname, '')

  return {
    plugins: [vue()],
    server: {
      proxy: {
        '/api/v1/auth': {
          target: env.VITE_AUTH_API_TARGET || 'http://localhost:8081',
          changeOrigin: true
        },
        '/api/v1/users': {
          target: env.VITE_USER_API_TARGET || 'http://localhost:8082',
          changeOrigin: true
        },
        '/api/v1/beta-applications': {
          target: env.VITE_USER_API_TARGET || 'http://localhost:8082',
          changeOrigin: true
        },
        '/nexus/api/beta-applications': {
          target: env.VITE_USER_API_TARGET || 'http://localhost:8082',
          changeOrigin: true
        },
        '/api/v1/feedbacks': {
          target: env.VITE_FEEDBACK_API_TARGET || 'http://localhost:8083',
          changeOrigin: true
        },
        '/api/v1/products': {
          target: env.VITE_PRODUCT_API_TARGET || 'http://localhost:8084',
          changeOrigin: true
        },
        '/api/v1/plans': {
          target: env.VITE_PLAN_API_TARGET || 'http://localhost:8085',
          changeOrigin: true
        },
        '/api/v1/subscriptions': {
          target: env.VITE_PLAN_API_TARGET || 'http://localhost:8085',
          changeOrigin: true
        },
        '/api/v1/audit': {
          target: env.VITE_AUDIT_API_TARGET || 'http://localhost:8087',
          changeOrigin: true
        }
      }
    }
  }
})

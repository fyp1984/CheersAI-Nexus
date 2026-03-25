import axios, { type AxiosError, type AxiosInstance, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ACCESS_TOKEN_KEY, REFRESH_TOKEN_KEY } from '../constants/auth'
import type { AuthResponse } from '../types/auth'
import { unwrapApiData } from './api'

declare module 'axios' {
  interface AxiosRequestConfig {
    skipAuth?: boolean
    _retry?: boolean
  }
}

export type ApiResponse<T = unknown> = {
  code: number
  message: string
  data: T
}

type RequestConfig = InternalAxiosRequestConfig & {
  skipAuth?: boolean
}

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/',
  timeout: 15000
})

const refreshClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const nextConfig = config as RequestConfig
  if (nextConfig.skipAuth) return nextConfig

  // Prevent duplicated '/api' when VITE_API_BASE_URL='/api' and request url starts with '/api/...'.
  const base = (nextConfig.baseURL || '').replace(/\/+$/, '')
  if (base.endsWith('/api') && typeof nextConfig.url === 'string' && nextConfig.url.startsWith('/api/')) {
    nextConfig.url = nextConfig.url.replace(/^\/api/, '')
  }

  const accessToken = localStorage.getItem(ACCESS_TOKEN_KEY)
  const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY)

  if (accessToken) {
    nextConfig.headers.set('Authorization', `Bearer ${accessToken}`)
  }
  if (refreshToken) {
    nextConfig.headers.set('X-Refresh-Token', refreshToken)
  }

  return nextConfig
})

request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => response,
  async (error: AxiosError<ApiResponse>) => {
    const originalRequest = error.config as RequestConfig | undefined
    const status = error.response?.status
    const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY)

    if (
      originalRequest &&
      !originalRequest.skipAuth &&
      !originalRequest._retry &&
      refreshToken &&
      (status === 401 || status === 403)
    ) {
      originalRequest._retry = true

      try {
        const refreshResponse = await refreshClient.post('/api/v1/auth/refresh', null, {
          headers: {
            'X-Refresh-Token': refreshToken
          }
        })
        const authData = unwrapApiData<AuthResponse>(refreshResponse.data)
        localStorage.setItem(ACCESS_TOKEN_KEY, authData.accessToken)
        localStorage.setItem(REFRESH_TOKEN_KEY, authData.refreshToken)
        originalRequest.headers.set('Authorization', `Bearer ${authData.accessToken}`)
        originalRequest.headers.set('X-Refresh-Token', authData.refreshToken)
        return request(originalRequest)
      } catch (refreshError) {
        localStorage.removeItem(ACCESS_TOKEN_KEY)
        localStorage.removeItem(REFRESH_TOKEN_KEY)
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
        return Promise.reject(refreshError)
      }
    }

    return Promise.reject(error)
  }
)

export default request

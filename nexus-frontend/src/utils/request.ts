import axios, { type AxiosError, type AxiosInstance, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ACCESS_TOKEN_KEY, REFRESH_TOKEN_KEY } from '../constants/auth'

declare module 'axios' {
  interface AxiosRequestConfig {
    skipAuth?: boolean
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

request.interceptors.request.use((config) => {
  const nextConfig = config as RequestConfig
  if (nextConfig.skipAuth) return nextConfig

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
  (error: AxiosError<ApiResponse>) => Promise.reject(error)
)

export default request

import request from '../utils/request'
import type { AuthResponse, UserInfo } from '../types/auth'
import { unwrapApiData } from '../utils/api'

export type SendCodeRequest = {
  email?: string
  phone?: string
  purpose: 'register' | 'login' | 'reset_password' | 'bind'
}

export type RegisterRequest = {
  email?: string
  phone?: string
  username: string
  password: string
  code: string
}

export type LoginRequest = {
  email?: string
  phone?: string
  password: string
}

export async function sendCode(payload: SendCodeRequest) {
  const response = await request.post('/api/v1/auth/send-code', payload, { skipAuth: true })
  return unwrapApiData<null>(response.data)
}

export async function register(payload: RegisterRequest) {
  const response = await request.post('/api/v1/auth/register', payload, { skipAuth: true })
  return unwrapApiData<AuthResponse>(response.data)
}

export async function login(payload: LoginRequest) {
  const response = await request.post('/api/v1/auth/login', payload, { skipAuth: true })
  return unwrapApiData<AuthResponse>(response.data)
}

export async function logout() {
  const response = await request.post('/api/v1/auth/logout')
  return unwrapApiData<null>(response.data)
}

export async function refreshAuthToken() {
  const response = await request.post('/api/v1/auth/refresh')
  return unwrapApiData<AuthResponse>(response.data)
}

export async function fetchCurrentUser() {
  const response = await request.get('/api/v1/auth/me')
  return unwrapApiData<UserInfo>(response.data)
}

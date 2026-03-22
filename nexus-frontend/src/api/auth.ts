import request, { type ApiResponse } from '../utils/request'

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

export type UserInfo = {
  id: string
  email?: string
  phone?: string
  username?: string
  nickname?: string
  avatarUrl?: string
}

export type AuthResponse = {
  accessToken: string
  refreshToken: string
  idToken: string
  expiresIn: number
  tokenType: string
  user: UserInfo
}

export async function sendCode(payload: SendCodeRequest) {
  return request.post<ApiResponse<null>>('/api/v1/auth/send-code', payload, { skipAuth: true })
}

export async function register(payload: RegisterRequest) {
  return request.post<ApiResponse<AuthResponse>>('/api/v1/auth/register', payload, { skipAuth: true })
}

export async function login(payload: LoginRequest) {
  return request.post<ApiResponse<AuthResponse>>('/api/v1/auth/login', payload, { skipAuth: true })
}

export async function logout() {
  return request.post<ApiResponse<null>>('/api/v1/auth/logout')
}

import request from '../utils/request'
import type { ApiResponse } from '../utils/request'

export function get<T>(url: string, params?: Record<string, unknown>) {
  return request.get<ApiResponse<T>>(url, { params })
}

export function post<T>(url: string, data?: Record<string, unknown>) {
  return request.post<ApiResponse<T>>(url, data)
}

export function put<T>(url: string, data?: Record<string, unknown>) {
  return request.put<ApiResponse<T>>(url, data)
}

export function del<T>(url: string, data?: Record<string, unknown>) {
  return request.delete<ApiResponse<T>>(url, { data })
}

export function unwrapData<T>(response: { data: ApiResponse<T> }): T {
  return response.data.data
}

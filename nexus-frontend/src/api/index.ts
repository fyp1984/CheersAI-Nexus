import request from '../utils/request'

export function get<T>(url: string, params?: Record<string, unknown>) {
  return request.get<T>(url, { params })
}

export function post<T>(url: string, data?: Record<string, unknown>) {
  return request.post<T>(url, data)
}

export function put<T>(url: string, data?: Record<string, unknown>) {
  return request.put<T>(url, data)
}

export function del<T>(url: string, data?: Record<string, unknown>) {
  return request.delete<T>(url, { data })
}

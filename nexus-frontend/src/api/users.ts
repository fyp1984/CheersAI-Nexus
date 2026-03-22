import request from '../utils/request'
import type { ApiResponse } from '../utils/request'

export type UserRecord = {
  userId: string
  email?: string
  phone?: string
  username?: string
  nickname?: string
  avatarUrl?: string
  status?: string
  emailVerified?: boolean
  phoneVerified?: boolean
  lastLoginAt?: string
  lastLoginIp?: string
  createdAt?: string
  updatedAt?: string
}

export type UserPage = {
  records: UserRecord[]
  pageNumber: number
  pageSize: number
  totalPage: number
  totalRow: number
}

export type UserListQuery = {
  pageNumber: number
  pageSize: number
  totalRow?: number
}

function parseResponseData<T>(rawData: unknown): T {
  const normalizeTypedWrapper = (value: unknown): unknown => {
    if (Array.isArray(value)) {
      if (value.length === 2 && typeof value[0] === 'string') {
        return normalizeTypedWrapper(value[1])
      }
      return value.map((item) => normalizeTypedWrapper(item))
    }
    if (value && typeof value === 'object') {
      const result: Record<string, unknown> = {}
      Object.entries(value as Record<string, unknown>).forEach(([key, item]) => {
        result[key] = normalizeTypedWrapper(item)
      })
      return result
    }
    return value
  }

  const parseMaybeJson = (value: unknown): unknown => {
    let current = value
    while (typeof current === 'string') {
      const text = current.trim()
      if (!(text.startsWith('{') || text.startsWith('['))) break
      try {
        current = JSON.parse(text)
      } catch {
        break
      }
    }
    return current
  }

  const normalizedRoot = normalizeTypedWrapper(rawData) as Record<string, unknown> | unknown[]
  const payload =
    normalizedRoot && typeof normalizedRoot === 'object' && !Array.isArray(normalizedRoot) && 'data' in normalizedRoot
      ? (normalizedRoot as Record<string, unknown>).data
      : normalizedRoot

  return normalizeTypedWrapper(parseMaybeJson(payload)) as T
}

export async function fetchUsers(query: UserListQuery) {
  const response = await request.get<ApiResponse<unknown>>('/api/v1/users', { params: query })
  return parseResponseData<UserPage>(response.data)
}

export async function searchUsers(userCondition: string) {
  const response = await request.get<ApiResponse<unknown>>('/api/v1/users/search', {
    params: { userCondition }
  })
  return parseResponseData<UserRecord[]>(response.data)
}

export async function fetchUserDetail(userId: string) {
  const response = await request.get<ApiResponse<unknown>>(`/api/v1/users/${userId}`)
  return parseResponseData<UserRecord>(response.data)
}

export async function updateUserStatus(userId: string, status: string) {
  const response = await request.put<ApiResponse<unknown>>(`/api/v1/users/${userId}`, null, {
    params: { status }
  })
  return parseResponseData<UserRecord>(response.data)
}

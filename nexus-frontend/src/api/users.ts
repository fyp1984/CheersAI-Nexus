import request from '../utils/request'
import { unwrapApiData } from '../utils/api'

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

export async function fetchUsers(query: UserListQuery) {
  const response = await request.get('/api/v1/users', { params: query })
  return unwrapApiData<UserPage>(response.data)
}

export async function searchUsers(userCondition: string) {
  const response = await request.get('/api/v1/users/search', {
    params: { userCondition }
  })
  return unwrapApiData<UserRecord[]>(response.data)
}

export async function fetchUserDetail(userId: string) {
  const response = await request.get(`/api/v1/users/${userId}`)
  return unwrapApiData<UserRecord>(response.data)
}

export async function updateUserStatus(userId: string, status: string) {
  const response = await request.put(`/api/v1/users/${userId}`, null, {
    params: { status }
  })
  return unwrapApiData<UserRecord>(response.data)
}

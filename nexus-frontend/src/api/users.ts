import request from '../utils/request'
import { unwrapApiData } from '../utils/api'

export type UserStatus = 'active' | 'inactive' | 'disabled' | 'deleted'
export type UserRole = 'user' | 'support' | 'operator' | 'admin'
export type MemberPlanCode = 'free' | 'pro' | 'pro_team' | 'enterprise'

export type UserRecord = {
  userId: string
  email?: string
  phone?: string
  username?: string
  nickname?: string
  avatarUrl?: string
  status?: UserStatus
  role?: UserRole
  memberPlanCode?: MemberPlanCode
  memberExpireAt?: string
  emailVerified?: boolean
  phoneVerified?: boolean
  lastLoginAt?: string
  lastLoginIp?: string
  createdAt?: string
  updatedAt?: string
}

export type UserPage = {
  items: UserRecord[]
  total: number
}

export type UserListQuery = {
  keyword?: string
  status?: UserStatus
  role?: UserRole
  memberPlanCode?: MemberPlanCode
  page?: number
  pageSize?: number
}

export type UserCreateDTO = {
  username: string
  nickname: string
  email?: string
  phone?: string
  password: string
  status?: UserStatus
  role?: UserRole
  memberPlanCode?: MemberPlanCode
  memberExpireAt?: string
}

export type UserUpdateDTO = {
  username?: string
  nickname?: string
  email?: string
  phone?: string
  status?: UserStatus
  role?: UserRole
  memberPlanCode?: MemberPlanCode
  memberExpireAt?: string
}

export type UserBatchStatusUpdateDTO = {
  userIds: string[]
  status: UserStatus
}

export type ResetPasswordResponse = {
  resetTo: string
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

export async function createUser(data: UserCreateDTO) {
  const response = await request.post('/api/v1/users', data)
  return unwrapApiData<UserRecord>(response.data)
}

export async function updateUser(userId: string, data: UserUpdateDTO) {
  const response = await request.put(`/api/v1/users/${userId}`, data)
  return unwrapApiData<UserRecord>(response.data)
}

export async function updateUserStatus(userId: string, status: UserStatus) {
  const response = await request.patch(`/api/v1/users/${userId}/status`, null, {
    params: { status }
  })
  return unwrapApiData<UserRecord>(response.data)
}

export async function updateBatchUserStatus(data: UserBatchStatusUpdateDTO) {
  const response = await request.post('/api/v1/users/batch-status', data)
  return unwrapApiData<void>(response.data)
}

export async function resetUserPassword(userId: string) {
  const response = await request.post(`/api/v1/users/${userId}/reset-password`)
  return unwrapApiData<ResetPasswordResponse>(response.data)
}

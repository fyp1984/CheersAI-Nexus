import request from '../utils/request'

export interface DesktopMember {
  id: string
  ssoUserId: string
  email: string | null
  name: string | null
  avatarUrl: string | null
  status: string
  lastLoginAt: string | null
  lastLoginIp: string | null
  lastActiveAt: string | null
  appVersion: string | null
  createdAt: string
  updatedAt: string
}

export interface DesktopMemberStats {
  totalMembers: number
  dau: number
  wau: number
  mau: number
  todayEvents: number
  todayFeedbacks: number
}

export interface DesktopMemberListResult {
  items: DesktopMember[]
  total: number
}

export async function fetchDesktopMembers(params: {
  page?: number
  pageSize?: number
  keyword?: string
  status?: string
}) {
  const response = await request.get('/api/v1/desktop-members', { params })
  return response.data
}

export async function fetchDesktopMemberStats() {
  const response = await request.get('/api/v1/desktop-members/stats')
  return response.data
}

export async function fetchDesktopMemberDetail(id: string) {
  const response = await request.get(`/api/v1/desktop-members/${id}`)
  return response.data
}

export async function fetchDesktopMemberEvents(id: string, params: {
  page?: number
  pageSize?: number
}) {
  const response = await request.get(`/api/v1/desktop-members/${id}/events`, { params })
  return response.data
}

export async function fetchDesktopMemberLoginHistory(id: string, params: {
  page?: number
  pageSize?: number
}) {
  const response = await request.get(`/api/v1/desktop-members/${id}/login-history`, { params })
  return response.data
}

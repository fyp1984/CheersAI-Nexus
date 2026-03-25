import request from '../utils/request'
import { unwrapApiData } from '../utils/api'

// ========== 审计日志相关类型 ==========

export interface AuditLogDTO {
  id: string
  logType: string
  logTypeDesc: string
  action: string
  actionDesc: string
  operatorId?: string
  operatorName?: string
  targetType?: string
  targetId?: string
  beforeData?: any
  afterData?: any
  ipAddress?: string
  userAgent?: string
  result: string
  errorMessage?: string
  createdAt: string
}

export interface AuditLogQueryParams {
  logType?: string
  action?: string
  operatorId?: string
  operatorName?: string
  targetType?: string
  targetId?: string
  ipAddress?: string
  result?: string
  startTime?: string
  endTime?: string
  page?: number
  pageSize?: number
}

export interface AuditLogListResponse {
  list: AuditLogDTO[]
  total: number
  page: number
  pageSize: number
}

// 日志类型选项
export const LOG_TYPE_OPTIONS = [
  { label: '全部', value: '' },
  { label: '用户行为', value: 'user_action' },
  { label: '管理操作', value: 'admin_action' },
  { label: '系统事件', value: 'system_event' },
  { label: '安全事件', value: 'security_event' }
]

// 操作结果选项
export const RESULT_OPTIONS = [
  { label: '全部', value: '' },
  { label: '成功', value: 'success' },
  { label: '失败', value: 'failure' }
]

// ========== 审计日志 API ==========

/**
 * 查询审计日志列表
 */
export async function fetchAuditLogs(params: AuditLogQueryParams) {
  const response = await request.get('/api/v1/audit-logs', { params })
  return unwrapApiData<AuditLogListResponse>(response.data)
}

/**
 * 获取审计日志详情
 */
export async function fetchAuditLogDetail(id: string) {
  const response = await request.get(`/api/v1/audit-logs/${id}`)
  return unwrapApiData<AuditLogDTO>(response.data)
}

/**
 * 导出审计日志
 */
export async function exportAuditLogs(params: Omit<AuditLogQueryParams, 'page' | 'pageSize'>) {
  const response = await request.get('/api/v1/audit-logs/export', { 
    params,
    responseType: 'blob' as any 
  })
  return response
}

/**
 * 清理过期日志（仅管理员）
 */
export async function cleanExpiredLogs() {
  const response = await request.delete('/api/v1/audit-logs/cleanup')
  return unwrapApiData<number>(response.data)
}

// ========== 工具函数 ==========

/**
 * 下载导出的文件
 */
export function downloadExportedFile(blob: Blob, filename: string) {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

/**
 * 格式化日期时间
 */
export function formatDateTime(dateTime: string): string {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

/**
 * 格式化日期
 */
export function formatDate(dateTime: string): string {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleDateString('zh-CN')
}

import request from '../utils/request'
import { unwrapApiData } from '../utils/api'

// ========== 系统配置相关类型 ==========

export interface RegisterConfig {
  registerMethods: string[]
  forceEmailVerify: boolean
  needInviteCode: boolean
  defaultMemberPlan: string
  autoActivate: boolean
}

export interface SecurityConfig {
  loginMode: string
  enableCaptcha: boolean
  failLockThreshold: number
  lockMinutes: number
  enable2FA: boolean
  passwordPolicy: string[]
}

export interface TokenConfig {
  accessTokenHours: number
  refreshTokenDays: number
  maxSessionCount: number
  notifyLoginFromNewIp: boolean
  idleTimeoutMinutes: number
}

export interface IpWhitelistItem {
  ip: string
  remark: string
  createTime: string
}

export interface SystemConfigDTO {
  register: RegisterConfig
  security: SecurityConfig
  token: TokenConfig
  ipWhitelist: IpWhitelistItem[]
}

// ========== 系统配置 API ==========

/**
 * 获取所有系统配置
 */
export async function fetchSystemConfig() {
  const response = await request.get('/api/v1/system-config')
  return unwrapApiData<SystemConfigDTO>(response.data)
}

/**
 * 保存所有系统配置
 */
export async function saveSystemConfig(config: SystemConfigDTO) {
  const response = await request.put('/api/v1/system-config', config)
  return unwrapApiData<null>(response.data)
}

/**
 * 获取IP白名单
 */
export async function fetchIpWhitelist() {
  const response = await request.get('/api/v1/system-config/ip-whitelist')
  return unwrapApiData<{ ipWhitelist: IpWhitelistItem[] }>(response.data)
}

/**
 * 添加IP到白名单
 */
export async function addIpWhitelist(ip: string, remark?: string) {
  const response = await request.post('/api/v1/system-config/ip-whitelist', { ip, remark })
  return unwrapApiData<null>(response.data)
}

/**
 * 从白名单移除IP
 */
export async function removeIpWhitelist(ip: string) {
  const response = await request.delete(`/api/v1/system-config/ip-whitelist/${encodeURIComponent(ip)}`)
  return unwrapApiData<null>(response.data)
}
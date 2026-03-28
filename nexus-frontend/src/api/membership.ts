import request from '../utils/request'
import { unwrapApiData } from '../utils/api'

// ========== 会员计划相关类型 ==========

export interface PlanDetailDTO {
  id: string
  code: string
  name: string
  description?: string
  priceMonthly?: number
  priceYearly?: number
  currency: string
  features?: string
  limits?: string
  sortOrder: number
  status: string
  auditStatus?: string
  createdAt: string
  updatedAt: string
}

export interface PlanCreateDTO {
  code: string
  name: string
  description?: string
  priceMonthly?: number
  priceYearly?: number
  currency?: string
  features?: string
  limits?: string
  sortOrder?: number
  status?: string
}

export interface PlanUpdateDTO {
  name?: string
  description?: string
  priceMonthly?: number
  priceYearly?: number
  currency?: string
  features?: string
  limits?: string
  sortOrder?: number
  status?: string
  applyRemark?: string
}

export interface PlanAuditDTO {
  action: 'approve' | 'reject'
  auditRemark?: string
}

// ========== 订阅相关类型 ==========

export interface SubscriptionDetailDTO {
  id: string
  userId: string
  planCode: string
  planName: string
  status: string
  startDate: string
  endDate: string
  autoRenew: boolean
  paymentMethod?: string
  lastPaymentAt?: string
  createdAt: string
  updatedAt: string
}

export interface SubscriptionCreateDTO {
  userId: string
  planCode: string
  startDate: string
  endDate: string
  autoRenew?: boolean
  paymentMethod?: string
}

export interface SubscriptionAdjustDTO {
  planCode: string
  expireDays: number
  reason: string
}

export interface UserSubscriptionDTO {
  userId: string
  email?: string
  username?: string
  subscriptionId?: string
  currentPlanCode: string
  currentPlanName: string
  subscriptionStatus?: string
  planExpire?: string
  autoRenew?: boolean
}

// ========== 审计日志相关类型 ==========

export interface SubscriptionAuditLog {
  id: string
  subscriptionId: string
  userId: string
  operateType: string
  beforePlanCode?: string
  afterPlanCode?: string
  beforeEndDate?: string
  afterEndDate?: string
  reason?: string
  operatorId?: string
  operatorName?: string
  operatorIp?: string
  createdAt: string
}

// ========== 会员计划 API ==========

/**
 * 获取会员计划列表
 */
export async function fetchPlanList() {
  const response = await request.get('/api/v1/plans')
  return unwrapApiData<PlanDetailDTO[]>(response.data)
}

/**
 * 获取会员计划详情
 */
export async function fetchPlanDetail(code: string) {
  const response = await request.get(`/api/v1/plans/${code}`)
  return unwrapApiData<PlanDetailDTO>(response.data)
}

/**
 * 创建会员计划
 */
export async function createPlan(data: PlanCreateDTO) {
  const response = await request.post('/api/v1/plans', data)
  return unwrapApiData<null>(response.data)
}

/**
 * 更新会员计划
 */
export async function updatePlan(code: string, data: PlanUpdateDTO) {
  const response = await request.put(`/api/v1/plans/${code}`, data)
  return unwrapApiData<null>(response.data)
}

/**
 * 删除会员计划
 */
export async function deletePlan(code: string) {
  const response = await request.delete(`/api/v1/plans/${code}`)
  return unwrapApiData<null>(response.data)
}

/**
 * 审批会员计划变更
 */
export async function auditPlan(code: string, data: PlanAuditDTO) {
  const response = await request.post(`/api/v1/plans/${code}/audit`, data)
  return unwrapApiData<null>(response.data)
}

/**
 * 获取待审批的变更记录
 */
export async function fetchPendingAudit(code: string) {
  const response = await request.get(`/api/v1/plans/${code}/pending-audit`)
  return unwrapApiData<any>(response.data)
}

// ========== 订阅相关 API ==========

/**
 * 获取订阅列表
 */
export async function fetchSubscriptionList(params?: { userId?: string; status?: string }) {
  const response = await request.get('/api/v1/subscriptions', { params })
  return unwrapApiData<SubscriptionDetailDTO[]>(response.data)
}

/**
 * 获取订阅详情
 */
export async function fetchSubscriptionDetail(id: string) {
  const response = await request.get(`/api/v1/subscriptions/${id}`)
  return unwrapApiData<SubscriptionDetailDTO>(response.data)
}

/**
 * 创建订阅
 */
export async function createSubscription(data: SubscriptionCreateDTO) {
  const response = await request.post('/api/v1/subscriptions', data)
  return unwrapApiData<SubscriptionDetailDTO>(response.data)
}

/**
 * 更新订阅
 */
export async function updateSubscription(id: string, data: SubscriptionCreateDTO) {
  const response = await request.put(`/api/v1/subscriptions/${id}`, data)
  return unwrapApiData<null>(response.data)
}

/**
 * 获取用户当前会员信息
 */
export async function fetchUserSubscription(userId: string) {
  const response = await request.get(`/api/v1/users/${userId}/subscription`)
  return unwrapApiData<UserSubscriptionDTO>(response.data)
}

/**
 * 手动调整用户会员（运营操作）
 */
export async function adjustUserSubscription(userId: string, data: SubscriptionAdjustDTO) {
  const response = await request.post(`/api/v1/users/${userId}/subscription/adjust`, data)
  return unwrapApiData<UserSubscriptionDTO>(response.data)
}

/**
 * 获取用户订阅变更审计日志
 */
export async function fetchSubscriptionAuditLogs(userId: string) {
  const response = await request.get(`/api/v1/users/${userId}/subscription/audit-logs`)
  return unwrapApiData<SubscriptionAuditLog[]>(response.data)
}

// ========== 权益配置相关类型 ==========

export interface PlanBenefitsDTO {
  planCode: string
  planName: string
  benefits: BenefitItem[]
}

export interface BenefitItem {
  key: string
  name: string
  description?: string
  type: 'switch' | 'input' | 'unlimited'
  enabled: boolean
  value?: number
  min?: number
  unlimited?: boolean
  planCodes?: string[]
}

// ========== 权益配置 API ==========

/**
 * 获取会员计划权益配置
 */
export async function fetchPlanBenefits(code: string) {
  const response = await request.get(`/api/v1/plans/${code}/benefits`)
  return unwrapApiData<PlanBenefitsDTO>(response.data)
}

/**
 * 更新会员计划权益配置
 */
export async function updatePlanBenefits(code: string, benefits: BenefitItem[]) {
  const response = await request.put(`/api/v1/plans/${code}/benefits`, { benefits })
  return unwrapApiData<null>(response.data)
}

// ========== 会员计划操作日志相关类型 ==========

export interface PlanAuditLogItem {
  id: string
  planId: string
  operateType: string
  auditStatus: string
  beforeData?: any
  afterData?: any
  applicantId?: string
  applicantName?: string
  applyRemark?: string
  auditorId?: string
  auditorName?: string
  auditRemark?: string
  appliedAt?: string
  auditedAt?: string
  createdAt: string
}

export interface PlanAuditLogsResponse {
  planCode: string
  planName: string
  items: PlanAuditLogItem[]
  total: number
}

// ========== 会员计划操作日志 API ==========

/**
 * 获取会员计划操作日志
 */
export async function fetchPlanAuditLogs(code: string) {
  const response = await request.get(`/api/v1/plans/${code}/audit-logs`)
  return unwrapApiData<PlanAuditLogsResponse>(response.data)
}

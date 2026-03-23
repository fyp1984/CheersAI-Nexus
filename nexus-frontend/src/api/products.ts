import request from '../utils/request'
import { unwrapApiData } from '../utils/api'

// ========== 产品相关类型 ==========

export interface ProductDetailDTO {
  id: string
  name: string
  code: string
  description?: string
  iconUrl?: string
  status: 'active' | 'inactive' | 'deprecated'
  currentVersion?: string
  downloadUrls?: string
  settings?: string
  createdAt: string
  updatedAt: string
}

export interface ProductCreateDTO {
  name: string
  code: string
  description?: string
  iconUrl?: string
  status?: string
  currentVersion?: string
  downloadUrls?: string
  settings?: string
}

export interface ProductUpdateDTO {
  name?: string
  description?: string
  iconUrl?: string
  status?: string
  currentVersion?: string
  downloadUrls?: string
  settings?: string
}

// ========== 产品版本相关类型 ==========

export interface ProductVersionDetailDTO {
  id: string
  productId: string
  version: string
  versionName?: string
  status: 'draft' | 'published' | 'deprecated'
  changelog?: string
  downloadUrls?: string
  releaseNote?: string
  forceUpdate: boolean
  minVersion?: string
  publishedAt?: string
  createdAt: string
  createdBy?: string
  createdByName?: string
}

export interface ProductVersionCreateDTO {
  version: string
  versionName?: string
  changelog?: string
  downloadUrls?: string
  releaseNote?: string
  forceUpdate?: boolean
  minVersion?: string
}

// ========== 产品 API ==========

/**
 * 获取产品列表
 */
export async function fetchProductList() {
  const response = await request.get('/api/v1/products')
  return unwrapApiData<ProductDetailDTO[]>(response.data)
}

/**
 * 获取产品详情
 */
export async function fetchProductDetail(id: string) {
  const response = await request.get(`/api/v1/products/${id}`)
  return unwrapApiData<ProductDetailDTO>(response.data)
}

/**
 * 创建产品
 */
export async function createProduct(data: ProductCreateDTO) {
  const response = await request.post('/api/v1/products', data)
  return unwrapApiData<null>(response.data)
}

/**
 * 更新产品
 */
export async function updateProduct(id: string, data: ProductUpdateDTO) {
  const response = await request.put(`/api/v1/products/${id}`, data)
  return unwrapApiData<null>(response.data)
}

/**
 * 删除产品
 */
export async function deleteProduct(id: string) {
  const response = await request.delete(`/api/v1/products/${id}`)
  return unwrapApiData<null>(response.data)
}

/**
 * 更新产品状态
 */
export async function updateProductStatus(id: string, status: string) {
  const response = await request.patch(`/api/v1/products/${id}/status?status=${status}`)
  return unwrapApiData<null>(response.data)
}

// ========== 产品版本 API ==========

/**
 * 获取产品的版本列表
 */
export async function fetchProductVersionList(productId: string) {
  const response = await request.get(`/api/v1/products/${productId}/versions`)
  return unwrapApiData<ProductVersionDetailDTO[]>(response.data)
}

/**
 * 创建产品版本
 */
export async function createProductVersion(productId: string, data: ProductVersionCreateDTO) {
  const response = await request.post(`/api/v1/products/${productId}/versions`, data)
  return unwrapApiData<null>(response.data)
}

/**
 * 发布产品版本
 */
export async function publishProductVersion(productId: string, versionId: string) {
  const response = await request.post(`/api/v1/products/${productId}/versions/${versionId}/publish`)
  return unwrapApiData<null>(response.data)
}

/**
 * 废弃产品版本
 */
export async function deprecateProductVersion(productId: string, versionId: string) {
  const response = await request.post(`/api/v1/products/${productId}/versions/${versionId}/deprecate`)
  return unwrapApiData<null>(response.data)
}

/**
 * 删除产品版本
 */
export async function deleteProductVersion(productId: string, versionId: string) {
  const response = await request.delete(`/api/v1/products/${productId}/versions/${versionId}`)
  return unwrapApiData<null>(response.data)
}

/**
 * 获取产品最新版本
 */
export async function fetchLatestProductVersion(productId: string) {
  const response = await request.get(`/api/v1/products/${productId}/versions/latest`)
  return unwrapApiData<ProductVersionDetailDTO>(response.data)
}

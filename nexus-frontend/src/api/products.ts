import request from '../utils/request'
import { unwrapApiData } from '../utils/api'

export type ProductStatus = 'active' | 'inactive' | 'deprecated'

export interface ProductDetailDTO {
  id: string
  name: string
  code: string
  description?: string
  iconUrl?: string
  status: ProductStatus
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
  status?: ProductStatus
  currentVersion?: string
  downloadUrls?: string
  settings?: string
}

export interface ProductUpdateDTO {
  code?: string
  name?: string
  description?: string
  iconUrl?: string
  status?: ProductStatus
  currentVersion?: string
  downloadUrls?: string
  settings?: string
}

export interface ProductListQuery {
  keyword?: string
  status?: ProductStatus | ''
  startTime?: string
  endTime?: string
  currentVersion?: string
  page?: number
  pageSize?: number
}

export interface ProductListResponseDTO {
  items: ProductDetailDTO[]
  total: number
}

export interface ProductBatchDeleteDTO {
  ids: string[]
}

export interface ProductFeatureDTO {
  key: string
  name: string
  desc: string
  enabled: boolean
  planCodes: string[]
}

export interface ProductFeatureUpdateDTO {
  features: ProductFeatureDTO[]
}

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

export interface ProductVersionUpdateDTO extends ProductVersionCreateDTO {}

export interface ProductOperationLogDTO {
  id: string
  productId?: string
  productCode?: string
  productName?: string
  action: string
  targetType: string
  targetId?: string
  content?: string
  beforeData?: string
  afterData?: string
  operatorId?: string
  operatorName?: string
  ipAddress?: string
  createdAt: string
}

export interface ProductOperationLogQuery {
  keyword?: string
  startTime?: string
  endTime?: string
  page?: number
  pageSize?: number
}

export interface ProductOperationLogPageDTO {
  items: ProductOperationLogDTO[]
  total: number
}

export async function fetchProductList(params: ProductListQuery) {
  const response = await request.get('/api/v1/products', { params })
  return unwrapApiData<ProductListResponseDTO>(response.data)
}

export async function fetchProductDetail(id: string) {
  const response = await request.get(`/api/v1/products/${id}`)
  return unwrapApiData<ProductDetailDTO>(response.data)
}

export async function createProduct(data: ProductCreateDTO) {
  const response = await request.post('/api/v1/products', data)
  return unwrapApiData<null>(response.data)
}

export async function updateProduct(id: string, data: ProductUpdateDTO) {
  const response = await request.put(`/api/v1/products/${id}`, data)
  return unwrapApiData<null>(response.data)
}

export async function deleteProduct(id: string) {
  const response = await request.delete(`/api/v1/products/${id}`)
  return unwrapApiData<null>(response.data)
}

export async function batchDeleteProducts(data: ProductBatchDeleteDTO) {
  const response = await request.post('/api/v1/products/batch-delete', data)
  return unwrapApiData<null>(response.data)
}

export async function updateProductStatus(id: string, status: ProductStatus) {
  const response = await request.patch(`/api/v1/products/${id}/status`, null, { params: { status } })
  return unwrapApiData<null>(response.data)
}

export async function fetchProductFeatures(id: string) {
  const response = await request.get(`/api/v1/products/${id}/features`)
  return unwrapApiData<ProductFeatureUpdateDTO>(response.data)
}

export async function updateProductFeatures(id: string, data: ProductFeatureUpdateDTO) {
  const response = await request.put(`/api/v1/products/${id}/features`, data)
  return unwrapApiData<null>(response.data)
}

export async function fetchProductVersionList(productId: string) {
  const response = await request.get(`/api/v1/products/${productId}/versions`)
  return unwrapApiData<ProductVersionDetailDTO[]>(response.data)
}

export async function createProductVersion(productId: string, data: ProductVersionCreateDTO) {
  const response = await request.post(`/api/v1/products/${productId}/versions`, data)
  return unwrapApiData<null>(response.data)
}

export async function updateProductVersion(productId: string, versionId: string, data: ProductVersionUpdateDTO) {
  const response = await request.put(`/api/v1/products/${productId}/versions/${versionId}`, data)
  return unwrapApiData<null>(response.data)
}

export async function publishProductVersion(productId: string, versionId: string) {
  const response = await request.post(`/api/v1/products/${productId}/versions/${versionId}/publish`)
  return unwrapApiData<null>(response.data)
}

export async function deprecateProductVersion(productId: string, versionId: string) {
  const response = await request.post(`/api/v1/products/${productId}/versions/${versionId}/deprecate`)
  return unwrapApiData<null>(response.data)
}

export async function deleteProductVersion(productId: string, versionId: string) {
  const response = await request.delete(`/api/v1/products/${productId}/versions/${versionId}`)
  return unwrapApiData<null>(response.data)
}

export async function fetchLatestProductVersion(productId: string) {
  const response = await request.get(`/api/v1/products/${productId}/versions/latest`)
  return unwrapApiData<ProductVersionDetailDTO>(response.data)
}

export async function fetchProductOperationLogs(params: ProductOperationLogQuery) {
  const response = await request.get('/api/v1/products/logs', { params })
  return unwrapApiData<ProductOperationLogPageDTO>(response.data)
}

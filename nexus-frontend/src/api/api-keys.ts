import request from '../utils/request'

export interface ApiKey {
  id: string
  keyId: string
  name: string
  status: string
  rateLimit: number
  createdAt: string
  expiresAt: string | null
  lastUsedAt: string | null
}

export async function fetchApiKeys() {
  const response = await request.get('/api/v1/api-keys')
  return response.data
}

export async function createApiKey(name: string) {
  const response = await request.post('/api/v1/api-keys', { name })
  return response.data
}

export async function revokeApiKey(id: string) {
  const response = await request.post(`/api/v1/api-keys/${id}/revoke`)
  return response.data
}

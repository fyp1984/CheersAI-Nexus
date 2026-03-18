import axios, { type AxiosError, type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import { decryptPayload, encryptPayload, signPayload, verifyPayloadSignature } from './crypto'

export type ApiResponse<T = unknown> = {
  code?: number
  message?: string
  result?: T
  sign?: string
  [key: string]: unknown
}

type SecureRequestConfig = InternalAxiosRequestConfig & {
  skipCrypto?: boolean
}

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
})

request.interceptors.request.use(async (config) => {
  const secureConfig = config as SecureRequestConfig
  const method = (secureConfig.method || 'get').toLowerCase()
  const shouldEncrypt =
    !secureConfig.skipCrypto &&
    ['post', 'put', 'patch', 'delete'].includes(method) &&
    secureConfig.data !== undefined &&
    !(secureConfig.data instanceof FormData)

  if (!shouldEncrypt) {
    return secureConfig
  }

  const originalData = secureConfig.data ?? {}
  const input = await encryptPayload(originalData)
  const sign = await signPayload(originalData)
  secureConfig.data = { input, sign }
  return secureConfig
})

request.interceptors.response.use(
  async (response) => {
    const secureConfig = response.config as SecureRequestConfig
    const body = response.data as ApiResponse

    if (
      !secureConfig.skipCrypto &&
      body &&
      typeof body === 'object' &&
      typeof body.result === 'string'
    ) {
      const decrypted = await decryptPayload(body.result)
      if (typeof body.sign === 'string' && body.sign.length > 0) {
        const isValid = await verifyPayloadSignature(decrypted, body.sign)
        if (!isValid) {
          throw new Error('Response signature verification failed')
        }
      }
      body.result = decrypted
    }

    response.data = body
    return response
  },
  (error: AxiosError) => Promise.reject(error)
)

export default request

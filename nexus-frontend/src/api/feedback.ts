import request from '../utils/request'
import type { ApiResponse } from '../utils/request'

export type FeedbackRecord = {
  id: string
  userId: string
  productId: string
  type: string
  title: string
  content: string
  attachments?: string
  status: string
  priority: string
  assigneeId?: string
  resolvedAt?: string
  createdAt?: string
  updatedAt?: string
}

export type FeedbackListQuery = {
  product?: string
  type?: string
  status?: string
}

export type FeedbackUpdatePayload = {
  status?: string
  priority?: string
}

export type FeedbackAssignPayload = {
  assigneeId: string
}

function parseResponseData<T>(rawData: unknown): T {
  const normalizeTypedWrapper = (value: unknown): unknown => {
    if (Array.isArray(value)) {
      if (value.length === 2 && typeof value[0] === 'string') {
        return normalizeTypedWrapper(value[1])
      }
      return value.map((item) => normalizeTypedWrapper(item))
    }

    if (value && typeof value === 'object') {
      const result: Record<string, unknown> = {}
      Object.entries(value as Record<string, unknown>).forEach(([key, item]) => {
        result[key] = normalizeTypedWrapper(item)
      })
      return result
    }

    return value
  }

  const parseMaybeJson = (value: unknown): unknown => {
    let current = value
    while (typeof current === 'string') {
      const text = current.trim()
      if (!(text.startsWith('{') || text.startsWith('['))) break
      try {
        current = JSON.parse(text)
      } catch {
        break
      }
    }
    return current
  }

  const normalizedRoot = normalizeTypedWrapper(rawData) as Record<string, unknown> | unknown[]
  const payload =
    normalizedRoot && typeof normalizedRoot === 'object' && !Array.isArray(normalizedRoot) && 'data' in normalizedRoot
      ? (normalizedRoot as Record<string, unknown>).data
      : normalizedRoot

  return normalizeTypedWrapper(parseMaybeJson(payload)) as T
}

export async function fetchFeedbackList(query: FeedbackListQuery) {
  const response = await request.get<ApiResponse<unknown>>('/api/v1/feedbacks', {
    params: query
  })
  return parseResponseData<FeedbackRecord[]>(response.data)
}

export async function fetchFeedbackDetail(id: string) {
  const response = await request.get<ApiResponse<unknown>>(`/api/v1/feedbacks/${id}`)
  return parseResponseData<FeedbackRecord>(response.data)
}

export async function updateFeedback(id: string, payload: FeedbackUpdatePayload) {
  const response = await request.put<ApiResponse<unknown>>(`/api/v1/feedbacks/${id}`, payload, {
    headers: {
      'Content-Type': 'application/json'
    }
  })
  return parseResponseData<void>(response.data)
}

export async function assignFeedback(id: string, payload: FeedbackAssignPayload) {
  const response = await request.put<ApiResponse<unknown>>(`/api/v1/feedbacks/${id}/assign`, payload, {
    headers: {
      'Content-Type': 'application/json'
    }
  })
  return parseResponseData<void>(response.data)
}

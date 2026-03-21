import request from '../utils/request'
import { unwrapApiData } from '../utils/api'

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

export async function fetchFeedbackList(query: FeedbackListQuery) {
  const response = await request.get('/api/v1/feedbacks', {
    params: query
  })
  return unwrapApiData<FeedbackRecord[]>(response.data)
}

export async function fetchFeedbackDetail(id: string) {
  const response = await request.get(`/api/v1/feedbacks/${id}`)
  return unwrapApiData<FeedbackRecord>(response.data)
}

export async function updateFeedback(id: string, payload: FeedbackUpdatePayload) {
  const response = await request.put(`/api/v1/feedbacks/${id}`, payload, {
    headers: {
      'Content-Type': 'application/json'
    }
  })
  return unwrapApiData<void>(response.data)
}

export async function assignFeedback(id: string, payload: FeedbackAssignPayload) {
  const response = await request.put(`/api/v1/feedbacks/${id}/assign`, payload, {
    headers: {
      'Content-Type': 'application/json'
    }
  })
  return unwrapApiData<void>(response.data)
}

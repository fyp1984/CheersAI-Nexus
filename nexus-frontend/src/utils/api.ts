export type ApiEnvelope<T = unknown> = {
  code?: number
  message?: string
  data?: T
}

export class ApiBusinessError extends Error {
  code?: number

  constructor(message: string, code?: number) {
    super(message)
    this.name = 'ApiBusinessError'
    this.code = code
  }
}

function normalizeTypedWrapper(value: unknown): unknown {
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

function parseMaybeJson(value: unknown): unknown {
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

export function unwrapApiData<T>(payload: unknown): T {
  const normalizedRoot = normalizeTypedWrapper(payload) as ApiEnvelope<unknown> | unknown[] | unknown
  const envelope =
    normalizedRoot && typeof normalizedRoot === 'object' && !Array.isArray(normalizedRoot) && 'code' in normalizedRoot
      ? (normalizedRoot as ApiEnvelope<unknown>)
      : null

  if (envelope && envelope.code !== undefined && envelope.code !== 200) {
    throw new ApiBusinessError(envelope.message || '请求失败', envelope.code)
  }

  const rawData = envelope ? envelope.data : normalizedRoot
  return normalizeTypedWrapper(parseMaybeJson(rawData)) as T
}

export function getErrorMessage(error: unknown, fallback: string) {
  if (error instanceof ApiBusinessError) {
    return error.message || fallback
  }

  if (error && typeof error === 'object') {
    const maybeError = error as {
      message?: string
      response?: { data?: { message?: string } }
    }
    return maybeError.response?.data?.message || maybeError.message || fallback
  }

  return fallback
}

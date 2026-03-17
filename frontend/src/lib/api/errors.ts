export type ApiErrorResponse = {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
  traceId?: string | null
  details?: Record<string, unknown> | null
}

export class ApiError extends Error {
  status: number
  body: unknown

  constructor(message: string, status: number, body: unknown) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.body = body
  }
}

export function isApiError(err: unknown): err is ApiError {
  return err instanceof ApiError
}

export function getApiErrorResponse(err: ApiError): ApiErrorResponse | null {
  if (err.body && typeof err.body === 'object') {
    const anyBody = err.body as any

    if (typeof anyBody.status === 'number' && typeof anyBody.message === 'string') {
      return {
        timestamp: String(anyBody.timestamp ?? ''),
        status: anyBody.status,
        error: String(anyBody.error ?? ''),
        message: anyBody.message,
        path: String(anyBody.path ?? ''),
        traceId: anyBody.traceId ?? null,
        details: (anyBody.details as Record<string, unknown> | null) ?? null,
      }
    }
  }

  return null
}

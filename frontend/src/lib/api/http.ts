import { getAccessToken, setAccessToken } from '@/lib/api/authTokenStore'
import { ApiError } from '@/lib/api/errors'
import { singleFlightRefresh } from '@/lib/api/refreshManager'

type Json = Record<string, unknown> | unknown[] | string | number | boolean | null

const API_BASE_URL =
  typeof import.meta.env.VITE_API_BASE_URL === 'string'
    ? import.meta.env.VITE_API_BASE_URL
    : 'http://localhost:8080'

function toUrl(path: string) {
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  const base = API_BASE_URL.replace(/\/$/, '')
  if (!base) {
    return path.startsWith('/') ? path : `/${path}`
  }
  return `${base}${path.startsWith('/') ? '' : '/'}${path}`
}

async function parseBody(res: Response): Promise<unknown> {
  const contentType = res.headers.get('content-type') ?? ''
  if (contentType.includes('application/json')) return (await res.json()) as unknown
  const text = await res.text()
  return text.length ? text : null
}

export type RefreshAccessTokenFn = () => Promise<string | null>

let refreshAccessTokenFn: RefreshAccessTokenFn | null = null

/**
 * Injected by Auth feature. The http client uses this to refresh on 401.
 */
export function setRefreshAccessTokenFn(fn: RefreshAccessTokenFn) {
  refreshAccessTokenFn = fn
}

type RequestOptions = Omit<RequestInit, 'body' | 'headers'> & {
  headers?: Record<string, string>
  body?: Json
  /**
   * When true, includes cookies (needed for refresh cookie).
   */
  withCredentials?: boolean
  /**
   * Internal: to avoid infinite refresh loops.
   */
  _retry?: boolean
}

export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const token = getAccessToken()
  const headers: Record<string, string> = {
    ...(options.body !== undefined ? { 'content-type': 'application/json' } : {}),
    ...(options.headers ?? {}),
    ...(token ? { authorization: `Bearer ${token}` } : {}),
  }

  const res = await fetch(toUrl(path), {
    ...options,
    headers,
    credentials: options.withCredentials ? 'include' : 'same-origin',
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  })

  if (res.status !== 401) {
    if (!res.ok) {
      const body = await parseBody(res)
      throw new ApiError(`Request failed: ${res.status}`, res.status, body)
    }
    return (await parseBody(res)) as T
  }

  // 401: attempt refresh + retry once
  if (options._retry || !refreshAccessTokenFn) {
    const body = await parseBody(res)
    throw new ApiError(`Unauthorized: ${res.status}`, res.status, body)
  }

  const newToken = await singleFlightRefresh(refreshAccessTokenFn)
  setAccessToken(newToken)

  if (!newToken) {
    const body = await parseBody(res)
    throw new ApiError(`Unauthorized: ${res.status}`, res.status, body)
  }

  return await request<T>(path, { ...options, _retry: true })
}

export const http = {
  get: <T>(path: string, options?: RequestOptions) => request<T>(path, { ...options, method: 'GET' }),
  post: <T>(path: string, body?: Json, options?: RequestOptions) =>
    request<T>(path, { ...options, method: 'POST', body }),
  put: <T>(path: string, body?: Json, options?: RequestOptions) =>
    request<T>(path, { ...options, method: 'PUT', body }),
  delete: <T>(path: string, options?: RequestOptions) => request<T>(path, { ...options, method: 'DELETE' }),
}


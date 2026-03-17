import { getAccessToken } from '@/lib/api/authTokenStore'
import { ApiError } from '@/lib/api/errors'

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

// Kept for compatibility, but http client no longer auto-retries on 401.
// Refresh is now handled explicitly by AuthProvider on app load or when needed.
export function setRefreshAccessTokenFn(_fn: RefreshAccessTokenFn) {
  // no-op
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

  // No automatic retry on 401: if refresh was needed, AuthProvider should have
  // already called /api/auth/refresh on load. Any 401 here is treated as an
  // error and surfaced to the caller.
  if (!res.ok) {
    const body = await parseBody(res)
    throw new ApiError(
      res.status === 401 ? `Unauthorized: ${res.status}` : `Request failed: ${res.status}`,
      res.status,
      body,
    )
  }

  return (await parseBody(res)) as T
}

export const http = {
  get: <T>(path: string, options?: RequestOptions) => request<T>(path, { ...options, method: 'GET' }),
  post: <T>(path: string, body?: Json, options?: RequestOptions) =>
    request<T>(path, { ...options, method: 'POST', body }),
  put: <T>(path: string, body?: Json, options?: RequestOptions) =>
    request<T>(path, { ...options, method: 'PUT', body }),
  delete: <T>(path: string, options?: RequestOptions) => request<T>(path, { ...options, method: 'DELETE' }),
}


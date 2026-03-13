import { ApiError } from '@/lib/api/errors'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? 'http://localhost:8080'

function toUrl(path: string) {
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  return `${API_BASE_URL}${path.startsWith('/') ? '' : '/'}${path}`
}

async function parseBody(res: Response): Promise<unknown> {
  const contentType = res.headers.get('content-type') ?? ''
  if (contentType.includes('application/json')) return (await res.json()) as unknown
  const text = await res.text()
  return text.length ? text : null
}

export type LoginRequest = { email: string; password: string }
export type AuthResponse = { accessToken: string; accessExpiresInSeconds?: number }

/**
 * Login should set the refresh token cookie (HttpOnly) and return an access token.
 */
export async function loginApi(body: LoginRequest): Promise<AuthResponse> {
  const res = await fetch(toUrl('/api/auth/login'), {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(body),
  })

  if (!res.ok) throw new ApiError(`Login failed: ${res.status}`, res.status, await parseBody(res))
  return (await parseBody(res)) as AuthResponse
}

/**
 * Refresh should use cookies only. It must NOT recurse into the http 401 refresh logic.
 */
export async function refreshApi(): Promise<AuthResponse> {

  const res = await fetch(toUrl('/api/auth/refresh'), {
    method: 'POST',
    credentials: 'include',
  })



  if (!res.ok) throw new ApiError(`Refresh failed: ${res.status}`, res.status, await parseBody(res))
  return (await parseBody(res)) as AuthResponse
}

export async function logoutApi(): Promise<void> {
  const res = await fetch(toUrl('/api/auth/logout'), {
    method: 'POST',
    credentials: 'include',
  })

  if (!res.ok) throw new ApiError(`Logout failed: ${res.status}`, res.status, await parseBody(res))
}


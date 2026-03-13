import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import type { PropsWithChildren } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { loginApi, logoutApi, refreshApi, type LoginRequest } from '@/features/auth/services/authApi'
import { setAccessToken, subscribeAccessToken } from '@/lib/api/authTokenStore'
import { setRefreshAccessTokenFn } from '@/lib/api/http'
import { decodeJwtClaims } from '@/lib/utils/jwt'

type AuthStatus = 'checking' | 'authenticated' | 'unauthenticated'

type AuthContextValue = {
  status: AuthStatus
  accessToken: string | null
  claims: Record<string, unknown> | null
  login: (req: LoginRequest) => Promise<void>
  logout: () => Promise<void>
  refresh: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: PropsWithChildren) {
  const queryClient = useQueryClient()
  const [status, setStatus] = useState<AuthStatus>('checking')
  const [accessToken, setTokenState] = useState<string | null>(null)

  const claims = useMemo(() => (accessToken ? decodeJwtClaims(accessToken) : null), [accessToken])

  // Keep local state in sync with token store (single source for the http client).
  useEffect(() => subscribeAccessToken(setTokenState), [])

  // Provide refresh function to http client for 401->refresh->retry.
  useEffect(() => {
    setRefreshAccessTokenFn(async () => {
      try {
        const res = await refreshApi()
        return res.accessToken ?? null
      } catch {
        return null
      }
    })
  }, [])

  const refresh = async () => {
    setStatus((s) => (s === 'authenticated' ? s : 'checking'))
    try {
      const res = await refreshApi()
      setAccessToken(res.accessToken)
      setStatus('authenticated')
    } catch {
      setAccessToken(null)
      setStatus('unauthenticated')
    }
  }

  const login = async (req: LoginRequest) => {
    const res = await loginApi(req)
    setAccessToken(res.accessToken)
    setStatus('authenticated')
  }

  const logout = async () => {
    // Best-effort backend logout (endpoint may not exist yet)
    try {
      await logoutApi()
    } catch {
      // ignore
    } finally {
      setAccessToken(null)
      queryClient.clear()
      setStatus('unauthenticated')
    }
  }

  useEffect(() => {
    void refresh()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const value: AuthContextValue = {
    status,
    accessToken,
    claims,
    login,
    logout,
    refresh,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}


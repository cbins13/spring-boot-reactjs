import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '@/app/providers/AuthProvider'

export function RequireAdmin() {
  const { status, claims } = useAuth()
  const location = useLocation()
  const role = (claims?.role as string | undefined) ?? (claims?.['role'] as string | undefined)

  if (status === 'checking') {
    return (
      <div className="min-h-screen grid place-items-center">
        <div className="text-sm text-muted-foreground">Checking session…</div>
      </div>
    )
  }

  if (status === 'unauthenticated') {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  if (role !== 'ROLE_ADMIN') {
    return <Navigate to="/home" replace />
  }

  return <Outlet />
}


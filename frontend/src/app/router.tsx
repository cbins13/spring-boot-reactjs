import { createBrowserRouter, Navigate } from 'react-router-dom'
import { RequireAuth } from '@/app/routing/RequireAuth'
import { RequireAdmin } from '@/app/routing/RequireAdmin'
import { LoginPage } from '@/pages/LoginPage'
import { HomePage } from '@/pages/HomePage'
import { UsersPage } from '@/pages/UsersPage'

export const router = createBrowserRouter([
  { path: '/', element: <Navigate to="/home" replace /> },
  { path: '/login', element: <LoginPage /> },
  {
    element: <RequireAuth />,
    children: [
      { path: '/home', element: <HomePage /> },
      {
        element: <RequireAdmin />,
        children: [{ path: '/users', element: <UsersPage /> }],
      },
    ],
  },
])


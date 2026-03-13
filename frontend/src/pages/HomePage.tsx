import { Link } from 'react-router-dom'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/app/providers/AuthProvider'
import { buttonVariants, Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'

export function HomePage() {
  const { claims, logout } = useAuth()
  const navigate = useNavigate()

  return (
    <div className="max-w-3xl mx-auto p-6 space-y-6">
      <div className="flex items-center justify-between gap-4">
        <h1 className="text-2xl font-semibold">Home</h1>
        <Button
          variant="outline"
          onClick={() => {
            void logout().then(() => navigate('/login', { replace: true }))
          }}
        >
          Logout
        </Button>
      </div>

      <div className="rounded-lg border p-4">
        <div className="text-sm font-medium mb-2">JWT claims</div>
        <pre className="text-xs overflow-auto">{JSON.stringify(claims, null, 2)}</pre>
      </div>

      <div className="flex gap-3">
        <Link to="/users" className={cn(buttonVariants({ variant: 'default' }))}>
          Users
        </Link>
      </div>
    </div>
  )
}


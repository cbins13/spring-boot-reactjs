import { useState } from 'react'
import { Link } from 'react-router-dom'
import { buttonVariants, Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'
import { isApiError } from '@/lib/api/errors'
import { useUsers } from '@/features/users/hooks/useUsers'
import { UsersTable } from '@/features/users/components/UsersTable'
import { AddUserDialog } from '@/features/users/components/AddUserDialog'
import { EditUserDialog } from '@/features/users/components/EditUserDialog'
import { DeleteUserDialog } from '@/features/users/components/DeleteUserDialog'
import type { UserDto } from '@/features/users/types'

export function UsersPage() {
  const usersQuery = useUsers()
  const [isAddOpen, setIsAddOpen] = useState(false)
  const [editingUser, setEditingUser] = useState<UserDto | null>(null)
  const [deletingUser, setDeletingUser] = useState<UserDto | null>(null)

  if (usersQuery.isLoading) {
    return (
      <div className="max-w-5xl mx-auto p-6 space-y-6">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-semibold">Users</h1>
          <Link to="/home" className={cn(buttonVariants({ variant: 'outline' }))}>
            Back
          </Link>
        </div>
        <div className="text-sm text-muted-foreground">Loading…</div>
      </div>
    )
  }

  if (usersQuery.isError) {
    const err = usersQuery.error
    const status = isApiError(err) ? err.status : null

    return (
      <div className="max-w-5xl mx-auto p-6 space-y-6">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-semibold">Users</h1>
          <Link to="/home" className={cn(buttonVariants({ variant: 'outline' }))}>
            Back
          </Link>
        </div>

        {status === 403 ? (
          <div className="rounded-lg border p-4">
            <div className="font-medium">Not authorized</div>
            <div className="text-sm text-muted-foreground mt-1">
              This endpoint requires an ADMIN account in the backend.
            </div>
          </div>
        ) : (
          <div className="rounded-lg border p-4">
            <div className="font-medium">Failed to load users</div>
            <div className="text-sm text-muted-foreground mt-1">Try again later.</div>
          </div>
        )}
      </div>
    )
  }

  const users = usersQuery.data ?? []

  return (
    <div className="max-w-5xl mx-auto p-6 space-y-6">
      <div className="flex items-center justify-between gap-3">
        <h1 className="text-2xl font-semibold">Users</h1>
        <div className="flex items-center gap-2">
          <Button type="button" variant="default" onClick={() => setIsAddOpen(true)}>
            Add user
          </Button>
          <Link to="/home" className={cn(buttonVariants({ variant: 'outline' }))}>
            Back
          </Link>
        </div>
      </div>

      <UsersTable
        users={users}
        onEdit={(user) => setEditingUser(user)}
        onDelete={(user) => setDeletingUser(user)}
      />

      <AddUserDialog open={isAddOpen} onOpenChange={setIsAddOpen} />
      <EditUserDialog user={editingUser} open={!!editingUser} onOpenChange={(open) => !open && setEditingUser(null)} />
      <DeleteUserDialog
        user={deletingUser}
        open={!!deletingUser}
        onOpenChange={(open) => !open && setDeletingUser(null)}
      />
    </div>
  )
}


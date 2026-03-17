import { useEffect, useState } from 'react'
import { Dialog, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select } from '@/components/ui/select'
import { Button } from '@/components/ui/button'
import { isApiError } from '@/lib/api/errors'
import type { UpdateUserRequest, UserDto } from '@/features/users/types'
import { useUpdateUser } from '@/features/users/hooks/useUpdateUser'
import { ResultDialog } from '@/components/ui/result-dialog'

type EditUserDialogProps = {
  user: UserDto | null
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function EditUserDialog({ user, open, onOpenChange }: EditUserDialogProps) {
  const [email, setEmail] = useState('')
  const [role, setRole] = useState<'ROLE_USER' | 'ROLE_ADMIN'>('ROLE_USER')
  const [resetPassword, setResetPassword] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [showResult, setShowResult] = useState(false)
  const updateUser = useUpdateUser()

  useEffect(() => {
    if (user && open) {
      setEmail(user.email)
      const isAdmin = user.roles.includes('ROLE_ADMIN')
      setRole(isAdmin ? 'ROLE_ADMIN' : 'ROLE_USER')
      setResetPassword(false)
      setError(null)
    }
  }, [user, open])

  const onClose = () => {
    if (updateUser.isPending) return
    onOpenChange(false)
  }

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!user) return
    setError(null)

    const body: UpdateUserRequest = {}
    if (email && email !== user.email) body.email = email
    if (resetPassword) body.password = 'Password123!'
    const currentIsAdmin = user.roles.includes('ROLE_ADMIN')
    const desiredIsAdmin = role === 'ROLE_ADMIN'
    if (desiredIsAdmin !== currentIsAdmin) {
      body.role = role
    }

    try {
      await updateUser.mutateAsync({ id: user.id, body })
      setShowResult(true)
    } catch (err) {
      if (isApiError(err)) {
        const bodyErr = err.body as { message?: string } | null
        setError(bodyErr?.message ?? 'Failed to update user')
      } else {
        setError('Failed to update user')
      }
    }
  }

  if (!user) return null

  const isDirty =
    email !== user.email ||
    resetPassword ||
    user.roles.includes('ROLE_ADMIN') !== (role === 'ROLE_ADMIN')

  return (
    <>
      <Dialog open={open} onOpenChange={onOpenChange}>
        <form onSubmit={onSubmit} className="space-y-4">
          <DialogHeader>
            <DialogTitle>Edit user</DialogTitle>
          </DialogHeader>

        <div className="space-y-2">
          <Label htmlFor="email">Email</Label>
          <Input id="email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
        </div>

        <div className="space-y-2">
          <Label htmlFor="role">Role</Label>
          <Select
            id="role"
            value={role}
            onChange={(e) => setRole(e.target.value as 'ROLE_USER' | 'ROLE_ADMIN')}
          >
            <option value="ROLE_USER">User</option>
            <option value="ROLE_ADMIN">Admin</option>
          </Select>
        </div>

        <div className="space-y-2">
          <Label htmlFor="reset-password">Reset password</Label>
          <div className="flex items-center gap-2">
            <input
              id="reset-password"
              type="checkbox"
              checked={resetPassword}
              onChange={(e) => setResetPassword(e.target.checked)}
            />
            <span className="text-sm text-muted-foreground">
              Set password to the default <span className="font-mono">Password123!</span>
            </span>
          </div>
        </div>

        {error && <p className="text-sm text-destructive">{error}</p>}

        <DialogFooter>
          <Button type="button" variant="outline" onClick={onClose} disabled={updateUser.isPending}>
            Cancel
          </Button>
          <Button type="submit" disabled={updateUser.isPending || !isDirty}>
            {updateUser.isPending ? 'Saving…' : 'Save changes'}
          </Button>
        </DialogFooter>
      </form>
    </Dialog>

      <ResultDialog
        open={showResult}
        onOpenChange={setShowResult}
        title="User updated"
        description="The user has been updated successfully."
      />
    </>
  )
}


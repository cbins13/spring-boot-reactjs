import { useState } from 'react'
import { Dialog, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select } from '@/components/ui/select'
import { Button } from '@/components/ui/button'
import { isApiError } from '@/lib/api/errors'
import type { CreateUserRequest } from '@/features/users/types'
import { useCreateUser } from '@/features/users/hooks/useCreateUser'
import { ResultDialog } from '@/components/ui/result-dialog'

type AddUserDialogProps = {
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function AddUserDialog({ open, onOpenChange }: AddUserDialogProps) {
  const [form, setForm] = useState<CreateUserRequest>({
    email: '',
    password: '',
    role: 'ROLE_USER',
  })
  const [error, setError] = useState<string | null>(null)
  const [showResult, setShowResult] = useState(false)
  const createUser = useCreateUser()

  const onClose = () => {
    if (createUser.isPending) return
    onOpenChange(false)
    setError(null)
  }

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    try {
      await createUser.mutateAsync(form)
      setShowResult(true)
    } catch (err) {
      if (isApiError(err)) {
        const body = err.body as { message?: string } | null
        setError(body?.message ?? 'Failed to create user')
      } else {
        setError('Failed to create user')
      }
    }
  }

  return (
    <>
      <Dialog open={open} onOpenChange={onOpenChange}>
        <form onSubmit={onSubmit} className="space-y-4">
          <DialogHeader>
            <DialogTitle>Add user</DialogTitle>
          </DialogHeader>

        <div className="space-y-2">
          <Label htmlFor="email">Email</Label>
          <Input
            id="email"
            type="email"
            required
            value={form.email}
            onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="password">Password</Label>
          <Input
            id="password"
            type="password"
            required
            minLength={8}
            value={form.password}
            onChange={(e) => setForm((f) => ({ ...f, password: e.target.value }))}
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="role">Role</Label>
          <Select
            id="role"
            value={form.role}
            onChange={(e) => setForm((f) => ({ ...f, role: e.target.value as CreateUserRequest['role'] }))}
          >
            <option value="ROLE_USER">User</option>
            <option value="ROLE_ADMIN">Admin</option>
          </Select>
          <p className="text-xs text-muted-foreground">
            Users can have multiple roles; this selects the primary role on creation.
          </p>
        </div>

        {error && <p className="text-sm text-destructive">{error}</p>}

        <DialogFooter>
          <Button type="button" variant="outline" onClick={onClose} disabled={createUser.isPending}>
            Cancel
          </Button>
          <Button type="submit" disabled={createUser.isPending}>
            {createUser.isPending ? 'Creating…' : 'Create'}
          </Button>
        </DialogFooter>
      </form>
    </Dialog>

      <ResultDialog
        open={showResult}
        onOpenChange={setShowResult}
        title="User created"
        description="The user has been created successfully."
      />
    </>
  )
}


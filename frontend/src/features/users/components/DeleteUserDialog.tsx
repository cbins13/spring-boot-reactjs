import { Dialog, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { isApiError } from '@/lib/api/errors'
import type { UserDto } from '@/features/users/types'
import { useDeleteUser } from '@/features/users/hooks/useDeleteUser'
import { useState } from 'react'
import { ResultDialog } from '@/components/ui/result-dialog'

type DeleteUserDialogProps = {
  user: UserDto | null
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function DeleteUserDialog({ user, open, onOpenChange }: DeleteUserDialogProps) {
  const deleteUser = useDeleteUser()
  const [error, setError] = useState<string | null>(null)
  const [showResult, setShowResult] = useState(false)

  const onClose = () => {
    if (deleteUser.isPending) return
    onOpenChange(false)
    setError(null)
  }

  const onConfirm = async () => {
    if (!user) return
    setError(null)
    try {
      await deleteUser.mutateAsync({ id: user.id })
      setShowResult(true)
    } catch (err) {
      if (isApiError(err)) {
        const body = err.body as { message?: string } | null
        setError(body?.message ?? 'Failed to delete user')
      } else {
        setError('Failed to delete user')
      }
    }
  }

  if (!user) return null

  return (
    <>
      <Dialog open={open} onOpenChange={onOpenChange}>
        <div className="space-y-4">
          <DialogHeader>
            <DialogTitle>Delete user</DialogTitle>
          </DialogHeader>

          <p className="text-sm text-muted-foreground">
            Are you sure you want to delete <span className="font-mono">{user.email}</span>? This action cannot be
            undone.
          </p>

          {error && <p className="text-sm text-destructive">{error}</p>}

          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose} disabled={deleteUser.isPending}>
              Cancel
            </Button>
            <Button type="button" variant="destructive" onClick={onConfirm} disabled={deleteUser.isPending}>
              {deleteUser.isPending ? 'Deleting…' : 'Delete'}
            </Button>
          </DialogFooter>
        </div>
      </Dialog>

      <ResultDialog
        open={showResult}
        onOpenChange={setShowResult}
        title="User deleted"
        description="The user has been deleted successfully."
      />
    </>
  )
}


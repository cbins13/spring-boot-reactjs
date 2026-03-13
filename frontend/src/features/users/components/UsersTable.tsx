import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import type { UserDto } from '@/features/users/types'
import { buttonVariants } from '@/components/ui/button'
import { cn } from '@/lib/utils'

type UsersTableProps = {
  users: UserDto[]
  onEdit: (user: UserDto) => void
  onDelete: (user: UserDto) => void
}

export function UsersTable({ users, onEdit, onDelete }: UsersTableProps) {
  return (
    <div className="rounded-lg border overflow-hidden">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Email</TableHead>
            <TableHead className="w-[140px]">Role</TableHead>
            <TableHead className="w-[200px]">Created</TableHead>
            <TableHead className="w-[180px] text-right">Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {users.map((u) => (
            <TableRow key={u.id}>
              <TableCell>{u.email}</TableCell>
              <TableCell>{u.role}</TableCell>
              <TableCell className="text-xs text-muted-foreground">
                {u.createdAt ? new Date(u.createdAt).toLocaleString() : '—'}
              </TableCell>
              <TableCell className="text-right space-x-2">
                <button
                  type="button"
                  className={cn(buttonVariants({ variant: 'outline', size: 'sm' }))}
                  onClick={() => onEdit(u)}
                >
                  Edit
                </button>
                <button
                  type="button"
                  className={cn(buttonVariants({ variant: 'destructive', size: 'sm' }))}
                  onClick={() => onDelete(u)}
                >
                  Delete
                </button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  )
}


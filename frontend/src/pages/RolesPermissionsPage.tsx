import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { Button, buttonVariants } from '@/components/ui/button'
import { cn } from '@/lib/utils'
import { isApiError, getApiErrorResponse } from '@/lib/api/errors'
import type { RoleDto } from '@/features/rbac/types'
import {
  useCreatePermission,
  useCreateRole,
  useDeletePermission,
  useDeleteRole,
  usePermissions,
  useRoles,
  useUpdatePermission,
  useUpdateRole,
  useUpdateRolePermissions,
} from '@/features/rbac/hooks/useRbac'

function uniqSorted(xs: string[]) {
  return Array.from(new Set(xs)).sort((a, b) => a.localeCompare(b))
}

export function RolesPermissionsPage() {
  const rolesQuery = useRoles()
  const permsQuery = usePermissions()

  const createRole = useCreateRole()
  const updateRole = useUpdateRole()
  const deleteRole = useDeleteRole()
  const updateRolePerms = useUpdateRolePermissions()

  const createPerm = useCreatePermission()
  const updatePerm = useUpdatePermission()
  const deletePerm = useDeletePermission()

  const [selectedRoleId, setSelectedRoleId] = useState<number | null>(null)

  const [roleForm, setRoleForm] = useState<{ id?: number; name: string; description: string }>({
    name: '',
    description: '',
  })
  const [permForm, setPermForm] = useState<{ id?: number; name: string; description: string }>({
    name: '',
    description: '',
  })

  const [error, setError] = useState<string | null>(null)

  const roles = rolesQuery.data ?? []
  const permissions = permsQuery.data ?? []

  const selectedRole: RoleDto | null = useMemo(() => {
    if (selectedRoleId == null) return null
    return roles.find((r) => r.id === selectedRoleId) ?? null
  }, [roles, selectedRoleId])

  const [selectedRolePermissionNames, setSelectedRolePermissionNames] = useState<Set<string>>(new Set())

  useEffect(() => {
    setSelectedRolePermissionNames(new Set(selectedRole?.permissions ?? []))
  }, [selectedRole])

  const allPermissionNames = useMemo(() => {
    return uniqSorted(permissions.map((p) => p.name))
  }, [permissions])

  const onApiError = (err: unknown, fallback: string) => {
    if (isApiError(err)) {
      const parsed = getApiErrorResponse(err)
      setError(parsed?.message ?? fallback)
      return
    }
    setError(fallback)
  }

  const isBusy =
    rolesQuery.isLoading ||
    permsQuery.isLoading ||
    createRole.isPending ||
    updateRole.isPending ||
    deleteRole.isPending ||
    updateRolePerms.isPending ||
    createPerm.isPending ||
    updatePerm.isPending ||
    deletePerm.isPending

  return (
    <div className="max-w-6xl mx-auto p-6 space-y-6">
      <div className="flex items-center justify-between gap-3">
        <h1 className="text-2xl font-semibold">Roles & Permissions</h1>
        <Link to="/home" className={cn(buttonVariants({ variant: 'outline' }))}>
          Back
        </Link>
      </div>

      {error && <div className="rounded-lg border p-3 text-sm text-destructive">{error}</div>}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="rounded-lg border p-4 space-y-4">
          <div className="flex items-center justify-between">
            <div className="font-medium">Roles</div>
            <div className="text-xs text-muted-foreground">Click a role to edit permissions</div>
          </div>

          {rolesQuery.isError ? (
            <div className="text-sm text-muted-foreground">Failed to load roles</div>
          ) : roles.length === 0 ? (
            <div className="text-sm text-muted-foreground">No roles yet</div>
          ) : (
            <div className="space-y-2">
              {roles.map((r) => (
                <button
                  key={r.id}
                  type="button"
                  className={cn(
                    'w-full text-left rounded-md border px-3 py-2 hover:bg-muted/50',
                    selectedRoleId === r.id && 'bg-muted',
                  )}
                  onClick={() => {
                    setSelectedRoleId(r.id)
                    setRoleForm({ id: r.id, name: r.name, description: r.description ?? '' })
                    setError(null)
                  }}
                >
                  <div className="flex items-center justify-between">
                    <div className="font-medium">{r.name}</div>
                    <div className="text-xs text-muted-foreground">{r.permissions.length} perms</div>
                  </div>
                  {r.description ? <div className="text-xs text-muted-foreground mt-1">{r.description}</div> : null}
                </button>
              ))}
            </div>
          )}

          <form
            className="rounded-md border p-3 space-y-3"
            onSubmit={async (e) => {
              e.preventDefault()
              setError(null)
              try {
                if (roleForm.id) {
                  await updateRole.mutateAsync({ id: roleForm.id, body: { name: roleForm.name, description: roleForm.description } })
                } else {
                  await createRole.mutateAsync({ name: roleForm.name, description: roleForm.description })
                }
                setRoleForm({ name: '', description: '' })
              } catch (err) {
                onApiError(err, 'Failed to save role')
              }
            }}
          >
            <div className="text-sm font-medium">{roleForm.id ? 'Edit role' : 'Add role'}</div>
            <input
              className="w-full rounded-md border px-3 py-2 text-sm"
              placeholder="ROLE_MANAGER"
              value={roleForm.name}
              onChange={(e) => setRoleForm((f) => ({ ...f, name: e.target.value }))}
              required
              disabled={isBusy}
            />
            <input
              className="w-full rounded-md border px-3 py-2 text-sm"
              placeholder="Description (optional)"
              value={roleForm.description}
              onChange={(e) => setRoleForm((f) => ({ ...f, description: e.target.value }))}
              disabled={isBusy}
            />
            <div className="flex items-center gap-2">
              <Button type="submit" disabled={isBusy}>
                {roleForm.id ? 'Update' : 'Create'}
              </Button>
              {roleForm.id ? (
                <Button
                  type="button"
                  variant="destructive"
                  disabled={isBusy}
                  onClick={async () => {
                    setError(null)
                    try {
                      await deleteRole.mutateAsync(roleForm.id!)
                      if (selectedRoleId === roleForm.id) setSelectedRoleId(null)
                      setRoleForm({ name: '', description: '' })
                    } catch (err) {
                      onApiError(err, 'Failed to delete role')
                    }
                  }}
                >
                  Delete
                </Button>
              ) : null}
            </div>
          </form>
        </div>

        <div className="rounded-lg border p-4 space-y-4">
          <div className="font-medium">Role permissions</div>

          {!selectedRole ? (
            <div className="text-sm text-muted-foreground">Select a role to edit its permissions.</div>
          ) : permsQuery.isError ? (
            <div className="text-sm text-muted-foreground">Failed to load permissions</div>
          ) : (
            <form
              className="space-y-3"
              onSubmit={async (e) => {
                e.preventDefault()
                setError(null)
                try {
                  const selected = allPermissionNames.filter((name) => selectedRolePermissionNames.has(name))
                  await updateRolePerms.mutateAsync({ id: selectedRole.id, permissions: selected })
                } catch (err) {
                  onApiError(err, 'Failed to update role permissions')
                }
              }}
            >
              <div className="text-sm text-muted-foreground">
                Editing <span className="font-medium text-foreground">{selectedRole.name}</span>
              </div>

              <div className="max-h-[320px] overflow-auto rounded-md border p-2">
                {allPermissionNames.length === 0 ? (
                  <div className="text-sm text-muted-foreground p-2">No permissions yet.</div>
                ) : (
                  <div className="space-y-1">
                    {allPermissionNames.map((name) => {
                      const checked = selectedRolePermissionNames.has(name)
                      return (
                        <label key={name} className="flex items-center gap-2 text-sm px-2 py-1 rounded hover:bg-muted/50">
                          <input
                            type="checkbox"
                            checked={checked}
                            onChange={(e) => {
                              setSelectedRolePermissionNames((prev) => {
                                const next = new Set(prev)
                                if (e.target.checked) next.add(name)
                                else next.delete(name)
                                return next
                              })
                            }}
                            disabled={isBusy}
                          />
                          <span>{name}</span>
                        </label>
                      )
                    })}
                  </div>
                )}
              </div>

              <Button type="submit" disabled={isBusy}>
                Save permissions
              </Button>

              <div className="rounded-md border p-3 space-y-3">
                <div className="text-sm font-medium">Add permission</div>
                <input
                  className="w-full rounded-md border px-3 py-2 text-sm"
                  placeholder="VIEW_AUDIT_LOGS"
                  value={permForm.name}
                  onChange={(e) => setPermForm((f) => ({ ...f, name: e.target.value }))}
                  required
                  disabled={isBusy}
                />
                <input
                  className="w-full rounded-md border px-3 py-2 text-sm"
                  placeholder="Description (optional)"
                  value={permForm.description}
                  onChange={(e) => setPermForm((f) => ({ ...f, description: e.target.value }))}
                  disabled={isBusy}
                />
                <div className="flex items-center gap-2">
                  <Button
                    type="button"
                    disabled={isBusy}
                    onClick={async () => {
                      setError(null)
                      try {
                        await createPerm.mutateAsync({ name: permForm.name, description: permForm.description })
                        setPermForm({ name: '', description: '' })
                      } catch (err) {
                        onApiError(err, 'Failed to create permission')
                      }
                    }}
                  >
                    Create permission
                  </Button>
                </div>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  )
}

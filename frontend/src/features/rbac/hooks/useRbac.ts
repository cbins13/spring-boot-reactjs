import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  createPermissionApi,
  createRoleApi,
  deletePermissionApi,
  deleteRoleApi,
  listPermissionsApi,
  listRolesApi,
  updatePermissionApi,
  updateRoleApi,
  updateRolePermissionsApi,
} from '@/features/rbac/services/rbacApi'

export function useRoles() {
  return useQuery({ queryKey: ['roles'], queryFn: listRolesApi })
}

export function usePermissions() {
  return useQuery({ queryKey: ['permissions'], queryFn: listPermissionsApi })
}

export function useCreateRole() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: createRoleApi,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['roles'] }),
  })
}

export function useUpdateRole() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, body }: { id: number; body: { name: string; description?: string } }) => updateRoleApi(id, body),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['roles'] }),
  })
}

export function useDeleteRole() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: deleteRoleApi,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['roles'] }),
  })
}

export function useUpdateRolePermissions() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, permissions }: { id: number; permissions: string[] }) => updateRolePermissionsApi(id, permissions),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['roles'] }),
  })
}

export function useCreatePermission() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: createPermissionApi,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['permissions'] })
      qc.invalidateQueries({ queryKey: ['roles'] })
    },
  })
}

export function useUpdatePermission() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, body }: { id: number; body: { name: string; description?: string } }) => updatePermissionApi(id, body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['permissions'] })
      qc.invalidateQueries({ queryKey: ['roles'] })
    },
  })
}

export function useDeletePermission() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: deletePermissionApi,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['permissions'] })
      qc.invalidateQueries({ queryKey: ['roles'] })
    },
  })
}

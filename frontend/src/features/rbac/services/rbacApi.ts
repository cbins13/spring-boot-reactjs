import { http } from '@/lib/api/http'
import type { PermissionDto, RoleDto } from '@/features/rbac/types'

export function listRolesApi() {
  return http.get<RoleDto[]>('/api/admin/roles', { withCredentials: true })
}

export function createRoleApi(body: { name: string; description?: string }) {
  return http.post<RoleDto>('/api/admin/roles', body, { withCredentials: true })
}

export function updateRoleApi(id: number, body: { name: string; description?: string }) {
  return http.put<RoleDto>(`/api/admin/roles/${id}`, body, { withCredentials: true })
}

export function deleteRoleApi(id: number) {
  return http.delete<void>(`/api/admin/roles/${id}`, { withCredentials: true })
}

export function updateRolePermissionsApi(id: number, permissions: string[]) {
  return http.put<RoleDto>(`/api/admin/roles/${id}/permissions`, { permissions }, { withCredentials: true })
}

export function listPermissionsApi() {
  return http.get<PermissionDto[]>('/api/admin/permissions', { withCredentials: true })
}

export function createPermissionApi(body: { name: string; description?: string }) {
  return http.post<PermissionDto>('/api/admin/permissions', body, { withCredentials: true })
}

export function updatePermissionApi(id: number, body: { name: string; description?: string }) {
  return http.put<PermissionDto>(`/api/admin/permissions/${id}`, body, { withCredentials: true })
}

export function deletePermissionApi(id: number) {
  return http.delete<void>(`/api/admin/permissions/${id}`, { withCredentials: true })
}

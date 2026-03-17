export type RoleDto = {
  id: number
  name: string
  description?: string | null
  permissions: string[]
}

export type PermissionDto = {
  id: number
  name: string
  description?: string | null
}

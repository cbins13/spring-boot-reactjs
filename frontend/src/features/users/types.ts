export type UserDto = {
  id: number
  email: string
  roles: string[]
  /**
   * Optional list of effective permissions for the user.
   * Backend may choose to omit this; the UI should handle undefined gracefully.
   */
  permissions?: string[]
  createdAt?: string
  updatedAt?: string
}

export type CreateUserRequest = {
  email: string
  password: string
  role: 'ROLE_USER' | 'ROLE_ADMIN'
}

export type UpdateUserRequest = {
  email?: string
  password?: string
  role?: 'ROLE_USER' | 'ROLE_ADMIN'
}

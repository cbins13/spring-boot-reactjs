export type UserDto = {
  id: number
  email: string
  role: 'ROLE_USER' | 'ROLE_ADMIN'
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

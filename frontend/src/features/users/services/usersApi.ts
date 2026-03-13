import { http } from '@/lib/api/http'
import type { CreateUserRequest, UpdateUserRequest, UserDto } from '@/features/users/types'

export function listUsersApi() {
  return http.get<UserDto[]>('/api/users', { withCredentials: true })
}

export function createUserApi(body: CreateUserRequest) {
  return http.post<UserDto>('/api/users', body, { withCredentials: true })
}

export function updateUserApi(id: number, body: UpdateUserRequest) {
  return http.put<UserDto>(`/api/users/${id}`, body, { withCredentials: true })
}

export function deleteUserApi(id: number) {
  return http.delete<void>(`/api/users/${id}`, { withCredentials: true })
}


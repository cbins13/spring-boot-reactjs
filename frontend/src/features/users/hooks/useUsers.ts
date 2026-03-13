import { useQuery } from '@tanstack/react-query'
import { listUsersApi } from '@/features/users/services/usersApi'

export function useUsers() {
  return useQuery({
    queryKey: ['users'],
    queryFn: listUsersApi,
  })
}


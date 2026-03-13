import { useMutation, useQueryClient } from '@tanstack/react-query'
import { createUserApi } from '@/features/users/services/usersApi'
import type { CreateUserRequest, UserDto } from '@/features/users/types'

export function useCreateUser() {
  const queryClient = useQueryClient()

  return useMutation<UserDto, unknown, CreateUserRequest>({
    mutationFn: (body) => createUserApi(body),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['users'] })
    },
  })
}


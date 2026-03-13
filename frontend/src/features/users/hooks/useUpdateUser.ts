import { useMutation, useQueryClient } from '@tanstack/react-query'
import { updateUserApi } from '@/features/users/services/usersApi'
import type { UpdateUserRequest, UserDto } from '@/features/users/types'

type UpdateUserVariables = { id: number; body: UpdateUserRequest }

export function useUpdateUser() {
  const queryClient = useQueryClient()

  return useMutation<UserDto, unknown, UpdateUserVariables>({
    mutationFn: ({ id, body }) => updateUserApi(id, body),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['users'] })
    },
  })
}


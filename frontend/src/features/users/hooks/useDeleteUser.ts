import { useMutation, useQueryClient } from '@tanstack/react-query'
import { deleteUserApi } from '@/features/users/services/usersApi'

export function useDeleteUser() {
  const queryClient = useQueryClient()

  return useMutation<void, unknown, { id: number }>({
    mutationFn: ({ id }) => deleteUserApi(id),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['users'] })
    },
  })
}


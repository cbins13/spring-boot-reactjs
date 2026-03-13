import { QueryClient } from '@tanstack/react-query'

export function createQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: {
        retry: (failureCount, error) => {
          // Avoid loops on auth failures (401/403 are handled via UI + refresh strategy)
          if (typeof error === 'object' && error && 'status' in error) return false
          return failureCount < 2
        },
        refetchOnWindowFocus: false,
      },
    },
  })
}


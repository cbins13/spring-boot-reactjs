export type JwtClaims = Record<string, unknown>

function base64UrlDecode(input: string) {
  const base64 = input.replace(/-/g, '+').replace(/_/g, '/')
  // pad base64
  const padded = base64 + '='.repeat((4 - (base64.length % 4)) % 4)
  const decoded = atob(padded)
  return decoded
}

export function decodeJwtClaims(token: string): JwtClaims | null {
  const parts = token.split('.')
  if (parts.length !== 3) return null
  try {
    const json = base64UrlDecode(parts[1]!)
    return JSON.parse(json) as JwtClaims
  } catch {
    return null
  }
}


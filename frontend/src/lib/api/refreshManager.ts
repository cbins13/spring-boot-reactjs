type RefreshFn = () => Promise<string | null>

let inFlight: Promise<string | null> | null = null

/**
 * Ensure we only run one refresh at a time. Everyone awaits the same promise.
 */
export async function singleFlightRefresh(refreshFn: RefreshFn) {
  if (!inFlight) {
    inFlight = (async () => {
      try {
        return await refreshFn()
      } finally {
        inFlight = null
      }
    })()
  }
  return await inFlight
}


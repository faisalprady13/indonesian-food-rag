import { useEffect, useState } from 'react';

export function useAsyncData<T>(
  fetcher: (signal: AbortSignal) => Promise<T>,
  deps: unknown[],
  enabled = true,
) {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(enabled);

  useEffect(() => {
    if (!enabled) {
      return;
    }

    const controller = new AbortController();
    setLoading(true);

    fetcher(controller.signal)
      .then(setData)
      .catch((error) => {
        if (error.name !== 'AbortError' && error.name !== 'CanceledError') {
          console.error(error);
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      });

    return () => controller.abort();
  }, [enabled, ...deps]);

  return [data, loading, setData] as const;
}

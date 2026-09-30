export function loginWithProvider(provider: 'github' | 'google') {
  const apiOrigin = import.meta.env.VITE_API_URL || window.location.origin
  window.location.assign(`${apiOrigin}/oauth2/authorization/${provider}`)
}

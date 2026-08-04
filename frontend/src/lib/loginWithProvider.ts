export function loginWithProvider(provider: 'github' | 'google') {
  const host =
    window.location.host === 'localhost:5173' ? 'http://localhost:8080' : window.location.origin;

  window.open(host + '/oauth2/authorization/' + provider, '_self');
}

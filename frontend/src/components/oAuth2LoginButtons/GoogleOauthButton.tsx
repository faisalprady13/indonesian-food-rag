import { Button } from '@/components/ui/button.tsx';
import { loginWithProvider } from '@/lib/loginWithProvider.ts';
import GoogleIcon from '@/assets/GoogleIcon.tsx';

function GoogleOauthButton() {
  return (
    <Button variant="outline" type="button" onClick={() => loginWithProvider('google')}>
      <GoogleIcon />
      Login with Google
    </Button>
  );
}

export default GoogleOauthButton;

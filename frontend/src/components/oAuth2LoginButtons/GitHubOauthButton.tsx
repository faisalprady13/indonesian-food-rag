import { Button } from '@/components/ui/button.tsx';
import { loginWithProvider } from '@/lib/loginWithProvider.ts';
import GitHubIcon from '@/assets/GitHubIcon.tsx';

function GitHubOauthButton() {
  return (
    <Button variant="outline" type="button" onClick={() => loginWithProvider('github')}>
      <GitHubIcon />
      Login with GitHub
    </Button>
  );
}

export default GitHubOauthButton;

import { Separator } from '@/components/ui/separator.tsx';
import GitHubOauthButton from '@/components/oAuth2LoginButtons/GitHubOauthButton.tsx';
import GoogleOauthButton from '@/components/oAuth2LoginButtons/GoogleOauthButton.tsx';

function OAuth2LoginButtons() {
  return (
    <>
      <div className="flex items-center gap-2">
        <Separator className="flex-1" />
        <span className="text-muted-foreground text-sm">or</span>
        <Separator className="flex-1" />
      </div>
      <GitHubOauthButton />
      <GoogleOauthButton />
    </>
  );
}

export default OAuth2LoginButtons;

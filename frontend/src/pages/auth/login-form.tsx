import { cn } from '@/lib/utils.ts';
import { Button } from '@/components/ui/button.tsx';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card.tsx';
import {
  Field,
  FieldDescription,
  FieldError,
  FieldGroup,
  FieldLabel,
} from '@/components/ui/field.tsx';
import { Input } from '@/components/ui/input.tsx';
import OAuth2LoginButtons from '@/components/oAuth2LoginButtons/OAuth2LoginButtons.tsx';
import Logo from '@/assets/logo.webp';
import * as React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { login } from '@/queries';
import type { CurrentUser } from '@/types/User.ts';

type LoginFormProps = React.ComponentProps<'div'> & {
  onLoginSuccess: () => Promise<CurrentUser>;
};

const OAUTH_ERROR_MESSAGES: Record<string, string> = {
  email_already_registered:
    'This email is already registered with a different sign-in method. Try logging in with that method instead.',
  email_not_found: 'We could not find an email address for that account.',
  email_not_verified: 'That account email is not verified.',
};

function readOAuthError(): string | null {
  const code = new URLSearchParams(window.location.search).get('error');
  return code ? (OAUTH_ERROR_MESSAGES[code] ?? 'Login failed. Please try again.') : null;
}

export function LoginForm({ className, onLoginSuccess, ...props }: LoginFormProps) {
  const navigate = useNavigate();
  const [username, setUsername] = React.useState('');
  const [password, setPassword] = React.useState('');
  const [error, setError] = React.useState<string | null>(readOAuthError);
  const [submitting, setSubmitting] = React.useState(false);

  React.useEffect(() => {
    if (new URLSearchParams(window.location.search).has('error')) {
      window.history.replaceState({}, '', window.location.pathname);
    }
  }, []);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await login(username, password);
      await onLoginSuccess();
      navigate('/');
    } catch {
      setError('Invalid username or password');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className={cn('flex flex-col gap-6', className)} {...props}>
      <Card>
        <CardHeader>
          <img src={Logo} alt="Logo" className="mx-auto mb-2 h-12 w-auto" />
          <CardTitle>Login to your account</CardTitle>
          <CardDescription>Enter your username below to login to your account</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit}>
            <FieldGroup>
              <Field>
                <FieldLabel htmlFor="username">Username</FieldLabel>
                <Input
                  id="username"
                  type="text"
                  placeholder="johndoe"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  required
                />
              </Field>
              <Field>
                <div className="flex items-center">
                  <FieldLabel htmlFor="password">Password</FieldLabel>
                </div>
                <Input
                  id="password"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </Field>
              <Field>
                {error && <FieldError>{error}</FieldError>}
                <Button type="submit" disabled={submitting}>
                  {submitting ? 'Logging in...' : 'Login'}
                </Button>
                <OAuth2LoginButtons />
                <FieldDescription className="text-center">
                  Don&apos;t have an account? <Link to="/register">Sign up</Link>
                </FieldDescription>
              </Field>
            </FieldGroup>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}

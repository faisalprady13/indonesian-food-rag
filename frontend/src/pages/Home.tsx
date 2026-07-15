import { Card, CardDescription, CardHeader, CardTitle } from '@/components/ui/card.tsx';
import type { CurrentUser } from '@/lib/api.ts';
import Hero from '@/assets/hero.png';

type HomeProps = {
  user: CurrentUser;
};

export default function Home({ user }: Readonly<HomeProps>) {
  return (
    <div className="mx-auto flex w-full max-w-2xl flex-1 items-center justify-center">
      <Card>
        <CardHeader>
          <img src={Hero} alt="" className="mx-auto mb-2 h-32 w-auto" />
          <CardTitle>Welcome back, {user.username}!</CardTitle>
          <CardDescription>
            Glad to see you again. Explore your favorite Indonesian recipes and keep your profile up
            to date.
          </CardDescription>
        </CardHeader>
      </Card>
    </div>
  );
}

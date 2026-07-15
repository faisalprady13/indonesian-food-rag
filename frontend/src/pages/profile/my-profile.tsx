import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar.tsx';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card.tsx';
import { Separator } from '@/components/ui/separator.tsx';
import type { CurrentUser } from '@/lib/api.ts';

type MyProfileProps = {
  user: CurrentUser;
};

export default function MyProfile({ user }: Readonly<MyProfileProps>) {
  return (
    <div className="mx-auto w-full max-w-md">
      <Card>
        <CardHeader className="items-center text-center">
          <Avatar className="mx-auto mb-2 size-16">
            <AvatarImage src={user.imageUrl ?? undefined} alt={user.username} />
            <AvatarFallback className="text-lg">
              {user.username.charAt(0).toUpperCase()}
            </AvatarFallback>
          </Avatar>
          <CardTitle>{user.username}</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-3">
          <Separator />
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">Email</span>
            <span className="font-medium">{user.email}</span>
          </div>
          {user.provider && (
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Signed up with</span>
              <span className="font-medium capitalize">{user.provider}</span>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

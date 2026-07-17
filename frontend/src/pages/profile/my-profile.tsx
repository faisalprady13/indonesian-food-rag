import { EditableAvatar } from '@/components/editableAvatar/editableAvatar';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card.tsx';
import { Input } from '@/components/ui/input';
import { Separator } from '@/components/ui/separator.tsx';
import { updateUserApi, type CurrentUser } from '@/lib/api.ts';
import { useAppStore } from '@/store/appStore';
import { Check, Pen } from 'lucide-react';
import { useState } from 'react';

type MyProfileProps = {
  user: CurrentUser;
};

export default function MyProfile({ user }: Readonly<MyProfileProps>) {
  const [editable, setEditable] = useState(false);
  const [fullname, setFullname] = useState(user.fullname);

  const updateUser = useAppStore((state) => state.updateUser);

  const handleSubmit = async () => {
    const updatedUser = await updateUserApi({ ...user, fullname: fullname });
    updateUser(updatedUser);
    setFullname(fullname);
    setEditable(false);
  };

  return (
    <div className="mx-auto w-full max-w-md">
      <Card>
        <CardHeader className="flex flex-col items-center gap-2 text-center">
          <EditableAvatar
            fallback={user.username.charAt(0).toUpperCase()}
            src={user.imageUrl ?? undefined}
            alt={user.username}
          />
          <CardTitle>{user.username}</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-3">
          <Separator />
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">Email</span>
            <span className="font-medium">{user.email}</span>
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">Full Name</span>
            <div className="flex items-center gap-2">
              {editable ? (
                <Input
                  value={fullname}
                  onChange={(e) => setFullname(e.target.value)}
                  className="w-full max-w-30  max-h-6"
                />
              ) : (
                <span className="font-medium">{user.fullname}</span>
              )}
              {editable ? (
                <Button
                  variant="positive"
                  size="icon"
                  className="h-8 w-8 p-0 bg-green-500"
                  onClick={handleSubmit}
                >
                  <Check className="h-4 w-4 text-muted-foreground" />
                </Button>
              ) : (
                <Button
                  variant="info"
                  size="icon"
                  className="h-8 w-8 p-0 "
                  onClick={() => setEditable(!editable)}
                >
                  <Pen className="h-4 w-4 text-muted-foreground" />
                </Button>
              )}
            </div>
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

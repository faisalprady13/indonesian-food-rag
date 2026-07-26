import ConfirmDeleteModal from '@/components/confirmDeleteModal/ConfirmDeleteModal';
import { EditableAvatar } from '@/components/editableAvatar/editableAvatar';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card.tsx';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label.tsx';
import { Separator } from '@/components/ui/separator.tsx';
import { deleteUserApi, logout, updateUserApi, type CurrentUser } from '@/lib/api.ts';
import { useAppStore } from '@/store/appStore';
import { useState, type FormEvent } from 'react';

type MyProfileProps = {
  user: CurrentUser;
};

export default function MyProfile({ user }: Readonly<MyProfileProps>) {
  const setUser = useAppStore((state) => state.setUser);
  const updateUser = useAppStore((state) => state.updateUser);

  const [fullname, setFullname] = useState(user.fullname || '');
  const [error, setError] = useState<string | undefined>();
  const [saving, setSaving] = useState(false);

  const isChanged = fullname.trim() !== user.fullname;

  const handleFullnameChange = (value: string) => {
    setFullname(value);
    if (error && value.trim()) {
      setError(undefined);
    }
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    const trimmed = fullname.trim();
    if (!trimmed) {
      setError('Full name is required');
      return;
    }

    if (trimmed === user.fullname) {
      return;
    }
    setSaving(true);
    try {
      const updatedUser = await updateUserApi({ ...user, fullname: trimmed });
      updateUser(updatedUser);
    } catch (error) {
      console.error(error);
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: number, username: string) => {
    try {
      await deleteUserApi(id, username);
      logout();
      setUser(null);
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <div className="mx-auto mt-6 w-full max-w-md px-0 py-6 mt-18">
      <Card>
        <CardHeader className="flex flex-col items-center gap-2 text-center">
          <EditableAvatar fallback={user.username.charAt(0).toUpperCase()} alt={user.username} />
          <CardTitle>{user.username}</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-4">
          <Separator />
          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="fullname">Full name</Label>
              <Input
                id="fullname"
                value={fullname}
                onChange={(e) => handleFullnameChange(e.target.value)}
                aria-invalid={!!error}
              />
              {error && <p className="text-sm text-destructive">{error}</p>}
            </div>
            <div className="flex items-center justify-between gap-4 text-sm">
              <span className="text-muted-foreground">Email</span>
              <span className="truncate font-medium">{user.email}</span>
            </div>
            <div className="flex items-center justify-between gap-4 text-sm">
              <span className="text-muted-foreground">Signed up with</span>
              <span className="font-medium capitalize">{user.provider}</span>
            </div>
            <Button
              type="submit"
              variant="outline"
              size="sm"
              className="w-full"
              disabled={!isChanged || saving}
            >
              {saving ? 'Saving...' : 'Save changes'}
            </Button>
          </form>
        </CardContent>
        <CardFooter className="flex justify-end">
          <ConfirmDeleteModal
            username={user.username}
            onConfirm={() => handleDelete(user.id, user.username)}
          />
        </CardFooter>
      </Card>
    </div>
  );
}

import ConfirmDeleteModal from '@/components/confirmDeleteModal/ConfirmDeleteModal';
import { EditableAvatar } from '@/components/editableAvatar/editableAvatar';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card.tsx';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label.tsx';
import { Separator } from '@/components/ui/separator.tsx';
import { deleteUserApi, logout, updateUserApi, type CurrentUser } from '@/lib/api.ts';
import { useAppStore } from '@/store/appStore';
import { useActionState, useState } from 'react';

type MyProfileProps = {
  user: CurrentUser;
};

type FullnameState = {
  error?: string;
};

export default function MyProfile({ user }: Readonly<MyProfileProps>) {
  const setUser = useAppStore((state) => state.setUser);
  const updateUser = useAppStore((state) => state.updateUser);

  const [isEditing, setIsEditing] = useState(false);

  const [state, submitFullname, isSaving] = useActionState<FullnameState, FormData>(
    async (_previousState, formData) => {
      const trimmed = String(formData.get('fullname') ?? '').trim();

      if (!trimmed) {
        return { error: 'Full name is required' };
      }

      if (trimmed === user.fullname) {
        setIsEditing(false);
        return {};
      }

      try {
        const updatedUser = await updateUserApi({ ...user, fullname: trimmed });
        updateUser(updatedUser);
        setIsEditing(false);
      } catch (error) {
        console.error(error);
        return { error: 'Failed to update full name' };
      }

      return {};
    },
    {},
  );

  const handleEditClick = () => {
    setIsEditing(true);
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
    <div className="mx-auto w-full max-w-md px-0 py-6 mt-18">
      <Card>
        <CardHeader className="flex flex-col items-center gap-2 text-center">
          <EditableAvatar fallback={user.username.charAt(0).toUpperCase()} alt={user.username} />
          <CardTitle>{user.username}</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-4">
          <Separator />
          <form action={submitFullname} className="flex flex-col gap-4">
            {isEditing ? (
              <div className="flex flex-row gap-4">
                <Label htmlFor="fullname" className="shrink-0">
                  Full name
                </Label>
                <Input
                  id="fullname"
                  name="fullname"
                  defaultValue={user.fullname}
                  autoFocus
                  aria-invalid={!!state.error}
                />
                {state.error && <p className="text-sm text-destructive">{state.error}</p>}
              </div>
            ) : (
              <div className="flex items-center justify-between gap-4 text-sm">
                <span className="text-muted-foreground">Full name</span>
                <span className="truncate font-medium">{user.fullname}</span>
              </div>
            )}
            <div className="flex items-center justify-between gap-4 text-sm">
              <span className="text-muted-foreground">Email</span>
              <span className="truncate font-medium">{user.email}</span>
            </div>
            <div className="flex items-center justify-between gap-4 text-sm">
              <span className="text-muted-foreground">Signed up with</span>
              <span className="font-medium capitalize">{user.provider}</span>
            </div>
            {isEditing && (
              <Button
                type="submit"
                variant="outline"
                size="sm"
                className="w-full"
                disabled={isSaving}
              >
                {isSaving ? 'Saving...' : 'Save changes'}
              </Button>
            )}
          </form>
          {!isEditing && (
            <Button
              type="button"
              variant="outline"
              size="sm"
              className="w-full"
              onClick={handleEditClick}
            >
              Edit
            </Button>
          )}
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

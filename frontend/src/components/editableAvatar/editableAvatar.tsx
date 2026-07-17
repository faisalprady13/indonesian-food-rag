'use client';

import { useCallback, useRef, useState } from 'react';
import { Pencil, Upload, X } from 'lucide-react';

import { cn } from '@/lib/utils';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { updateUserApi } from '@/lib/api';
import { useAppStore } from '@/store/appStore';

interface EditableAvatarProps {
  src?: string;
  fallback?: string;
  alt?: string;
  className?: string;
}

export function EditableAvatar({
  fallback = 'ME',
  alt = 'Profile picture',
  className,
}: EditableAvatarProps) {
  const user = useAppStore((state) => state.user);
  const updateUser = useAppStore((state) => state.updateUser);
  const [open, setOpen] = useState(false);
  const [preview, setPreview] = useState<string | null>(user?.imageUrl ?? null);
  const [file, setFile] = useState<File | undefined>();
  const [dragActive, setDragActive] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  const handleFile = useCallback((file: File | undefined) => {
    if (!file || !file.type.startsWith('image/')) return;
    const reader = new FileReader();
    reader.onload = () => setPreview(reader.result as string);
    reader.readAsDataURL(file);
    setFile(file);
  }, []);

  if (!user) {
    return null;
  }

  const handleSave = async () => {
    if (file && user) {
      const updatedUser = await updateUserApi(user, file);
      updateUser(updatedUser);
    }
    setOpen(false);
  };

  const openModal = () => {
    setPreview(user.imageUrl);
    setOpen(true);
  };

  return (
    <>
      <button
        type="button"
        onClick={openModal}
        aria-label="Edit profile picture"
        className={cn(
          'group relative inline-flex rounded-full outline-none ring-offset-2 ring-offset-background focus-visible:ring-2 focus-visible:ring-ring',
          className,
        )}
      >
        <Avatar className="size-24 border border-border">
          <AvatarImage src={user.imageUrl ?? '/placeholder.svg'} alt={alt} />
          <AvatarFallback className="text-xl">{fallback}</AvatarFallback>
        </Avatar>
        <span className="absolute inset-0 flex items-center justify-center rounded-full bg-foreground/50 opacity-0 transition-opacity group-hover:opacity-100">
          <Pencil className="size-6 text-background" />
        </span>
      </button>

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Update profile picture</DialogTitle>
            <DialogDescription>Upload a new image to use as your avatar.</DialogDescription>
          </DialogHeader>

          <div className="flex flex-col items-center gap-4 py-2">
            <Avatar className="size-28 border border-border">
              <AvatarImage src={preview || '/placeholder.svg'} alt="Preview" />
              <AvatarFallback className="text-2xl">{fallback}</AvatarFallback>
            </Avatar>

            <div
              role="button"
              tabIndex={0}
              onClick={() => inputRef.current?.click()}
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') inputRef.current?.click();
              }}
              onDragOver={(e) => {
                e.preventDefault();
                setDragActive(true);
              }}
              onDragLeave={() => setDragActive(false)}
              onDrop={(e) => {
                e.preventDefault();
                setDragActive(false);
                handleFile(e.dataTransfer.files?.[0]);
              }}
              className={cn(
                'flex w-full cursor-pointer flex-col items-center justify-center gap-2 rounded-lg border border-dashed border-border p-6 text-center transition-colors hover:bg-accent',
                dragActive && 'border-ring bg-accent',
              )}
            >
              <Upload className="size-5 text-muted-foreground" />
              <p className="text-sm font-medium">Click to upload or drag and drop</p>
              <p className="text-xs text-muted-foreground">PNG, JPG or GIF</p>
              <input
                ref={inputRef}
                type="file"
                accept="image/*"
                className="sr-only"
                onChange={(e) => handleFile(e.target.files?.[0])}
              />
            </div>

            {preview && preview !== user.imageUrl && (
              <Button
                type="button"
                variant="ghost"
                size="sm"
                onClick={() => setPreview(user.imageUrl)}
                className="gap-1"
              >
                <X className="size-4" />
                Reset
              </Button>
            )}
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleSave} disabled={!preview || preview === user.imageUrl}>
              Save
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}

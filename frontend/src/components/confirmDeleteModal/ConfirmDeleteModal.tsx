import { Trash2 } from 'lucide-react';
import { useState } from 'react';

import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';

interface ConfirmDeleteModalProps {
  username: string;
  onConfirm: () => void | Promise<void>;
}

export default function ConfirmDeleteModal({
  username,
  onConfirm,
}: Readonly<ConfirmDeleteModalProps>) {
  const [open, setOpen] = useState(false);
  const [confirmText, setConfirmText] = useState('');

  const isMatch = confirmText === username;

  const handleOpenChange = (nextOpen: boolean) => {
    if (!nextOpen) setConfirmText('');
    setOpen(nextOpen);
  };

  const handleConfirm = async () => {
    if (!isMatch) return;
    await onConfirm();
    handleOpenChange(false);
  };

  return (
    <>
      <Button variant="destructive" size="sm" className="gap-1.5" onClick={() => setOpen(true)}>
        <Trash2 className="h-4 w-4" />
        DELETE
      </Button>

      <Dialog open={open} onOpenChange={handleOpenChange}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Delete account</DialogTitle>
            <DialogDescription>
              This action cannot be undone. Type{' '}
              <span className="font-medium text-foreground">{username}</span> to confirm.
            </DialogDescription>
          </DialogHeader>

          <Input
            value={confirmText}
            onChange={(e) => setConfirmText(e.target.value)}
            placeholder={username}
            autoFocus
          />

          <DialogFooter>
            <Button variant="outline" onClick={() => handleOpenChange(false)}>
              Cancel
            </Button>
            <Button variant="destructive" disabled={!isMatch} onClick={handleConfirm}>
              Delete account
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}

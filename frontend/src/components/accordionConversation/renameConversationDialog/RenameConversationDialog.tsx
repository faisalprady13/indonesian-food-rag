import { useActionState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog.tsx';
import { Button } from '@/components/ui/button.tsx';
import { Input } from '@/components/ui/input.tsx';
import { renameConversation } from '@/lib/api';
import type { Conversation } from '@/types/Chat.ts';

type RenameConversationDialogProps = {
  conversation: Conversation | null;
  onOpenChange: (open: boolean) => void;
};

type RenameState = {
  error?: string;
};

export default function RenameConversationDialog({
  conversation,
  onOpenChange,
}: Readonly<RenameConversationDialogProps>) {
  const queryClient = useQueryClient();

  const [state, submitRename, isSaving] = useActionState<RenameState, FormData>(
    async (_previousState, formData) => {
      if (!conversation) return {};

      const title = String(formData.get('title') ?? '').trim();
      if (!title) {
        return { error: 'Title is required' };
      }

      try {
        await renameConversation(conversation.id, title);
        await queryClient.invalidateQueries({ queryKey: ['chat', 'conversations'] });
        onOpenChange(false);
      } catch (error) {
        console.error(error);
        return { error: 'Failed to rename conversation' };
      }

      return {};
    },
    {},
  );

  return (
    <Dialog open={!!conversation} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Rename conversation</DialogTitle>
          <DialogDescription>Give this conversation a new title.</DialogDescription>
        </DialogHeader>
        <form key={conversation?.id} action={submitRename} className="flex flex-col gap-4">
          <Input
            name="title"
            defaultValue={conversation?.title}
            autoFocus
            aria-invalid={!!state.error}
          />
          {state.error && <p className="text-sm text-destructive">{state.error}</p>}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={isSaving}>
              {isSaving ? 'Saving...' : 'Save'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

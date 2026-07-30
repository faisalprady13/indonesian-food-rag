import { useActionState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
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
import { deleteConversation } from '@/queries';
import type { Conversation } from '@/types/Chat.ts';

type DeleteConversationDialogProps = {
  conversation: Conversation | null;
  onOpenChange: (open: boolean) => void;
};

type DeleteState = {
  error?: string;
};

export default function DeleteConversationDialog({
  conversation,
  onOpenChange,
}: Readonly<DeleteConversationDialogProps>) {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { conversationId } = useParams<{ conversationId: string }>();

  const [state, submitDelete, isDeleting] = useActionState<DeleteState, FormData>(async () => {
    if (!conversation) return {};

    try {
      await deleteConversation(conversation.id);
      await queryClient.invalidateQueries({ queryKey: ['chat', 'conversations'] });
      if (conversationId === String(conversation.id)) {
        navigate('/chat');
      }
      onOpenChange(false);
    } catch (error) {
      console.error(error);
      return { error: 'Failed to delete conversation' };
    }

    return {};
  }, {});

  return (
    <Dialog open={!!conversation} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Delete conversation</DialogTitle>
          <DialogDescription>
            This will permanently delete{' '}
            <span className="font-medium text-foreground">{conversation?.title}</span> and all of
            its messages. This action cannot be undone.
          </DialogDescription>
        </DialogHeader>
        <form action={submitDelete} className="flex flex-col gap-2">
          {state.error && <p className="text-sm text-destructive">{state.error}</p>}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="destructive" disabled={isDeleting}>
              {isDeleting ? 'Deleting...' : 'Delete'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

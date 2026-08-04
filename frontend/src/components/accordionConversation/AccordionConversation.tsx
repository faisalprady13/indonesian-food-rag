import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  Accordion,
  AccordionItem,
  AccordionTrigger,
  AccordionPanel,
} from '@/components/ui/accordion.tsx';
import ConversationRow from '@/components/accordionConversation/conversationRow/ConversationRow.tsx';
import RenameConversationDialog from '@/components/accordionConversation/renameConversationDialog/RenameConversationDialog.tsx';
import DeleteConversationDialog from '@/components/accordionConversation/deleteConversationDialog/DeleteConversationDialog.tsx';
import { getConversations, updateConversationPinned } from '@/queries';
import { cn } from '@/lib/utils';
import type { Conversation } from '@/types/Chat.ts';
import ConversationListSkeleton from '@/components/skeletons/ConversationListSkeleton.tsx';

const triggerClass = 'px-1 text-xs font-semibold tracking-wide text-muted-foreground uppercase';

type AccordionConversationProps = {
  enabled?: boolean;
  onNavigate?: () => void;
  className?: string;
};

function byNewestFirst(a: Conversation, b: Conversation) {
  return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime();
}

export default function AccordionConversation({
  enabled = true,
  onNavigate,
  className,
}: Readonly<AccordionConversationProps>) {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { conversationId } = useParams<{ conversationId: string }>();

  const { data: conversations, isLoading } = useQuery({
    queryKey: ['chat', 'conversations'],
    queryFn: ({ signal }) => getConversations(signal),
    enabled,
  });

  const [renameTarget, setRenameTarget] = useState<Conversation | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Conversation | null>(null);

  const { mutate: togglePinned } = useMutation({
    mutationFn: ({ id, pinned }: { id: number; pinned: boolean }) =>
      updateConversationPinned(id, pinned),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['chat', 'conversations'] }),
  });

  function handleSelectConversation(id: number) {
    navigate(`/chat/${id}`);
    onNavigate?.();
  }

  const pinnedConversations = (conversations ?? [])
    .filter((conversation) => conversation.pinned)
    .sort(byNewestFirst);
  const unpinnedConversations = (conversations ?? [])
    .filter((conversation) => !conversation.pinned)
    .sort(byNewestFirst);

  function renderConversationRow(conversation: Conversation) {
    return (
      <ConversationRow
        key={conversation.id}
        conversation={conversation}
        isActive={conversationId === String(conversation.id)}
        onSelect={handleSelectConversation}
        onTogglePinned={(target) => togglePinned({ id: target.id, pinned: !target.pinned })}
        onRename={setRenameTarget}
        onDelete={setDeleteTarget}
      />
    );
  }

  return (
    <>
      <Accordion multiple defaultValue={['pinned', 'conversations']}>
        {pinnedConversations.length > 0 && (
          <AccordionItem value="pinned">
            <AccordionTrigger className={cn(triggerClass, className)}>Pinned</AccordionTrigger>
            <AccordionPanel>
              <div className="flex flex-col gap-1">
                {pinnedConversations.map(renderConversationRow)}
              </div>
            </AccordionPanel>
          </AccordionItem>
        )}

        <AccordionItem value="conversations">
          <AccordionTrigger className={cn(triggerClass, className)}>Conversations</AccordionTrigger>
          <AccordionPanel>
            <div className="flex flex-col gap-1">
              {isLoading ? (
                <ConversationListSkeleton />
              ) : unpinnedConversations.length > 0 ? (
                unpinnedConversations.map(renderConversationRow)
              ) : conversations && conversations.length > 0 ? null : (
                <p className="px-2.5 py-1.5 text-sm text-muted-foreground">No conversations yet.</p>
              )}
            </div>
          </AccordionPanel>
        </AccordionItem>
      </Accordion>

      <RenameConversationDialog
        conversation={renameTarget}
        onOpenChange={(open) => {
          if (!open) setRenameTarget(null);
        }}
      />

      <DeleteConversationDialog
        conversation={deleteTarget}
        onOpenChange={(open) => {
          if (!open) setDeleteTarget(null);
        }}
      />
    </>
  );
}

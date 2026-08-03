import { Ellipsis, Pin, PinOff } from 'lucide-react';
import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  Accordion,
  AccordionItem,
  AccordionTrigger,
  AccordionPanel,
} from '@/components/ui/accordion.tsx';
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
} from '@/components/ui/dropdown-menu.tsx';
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
    const isActive = conversationId === String(conversation.id);
    return (
      <div
        key={conversation.id}
        className={`flex items-center rounded-md text-sm transition-colors ${
          isActive
            ? 'bg-muted text-foreground'
            : 'text-muted-foreground hover:bg-muted hover:text-foreground'
        }`}
      >
        <button
          type="button"
          onClick={() => handleSelectConversation(conversation.id)}
          className="min-w-0 flex-1 truncate px-2.5 py-1.5 text-left"
        >
          {conversation.title}
        </button>
        <button
          type="button"
          aria-label={conversation.pinned ? `Unpin ${conversation.title}` : `Pin ${conversation.title}`}
          onClick={() => togglePinned({ id: conversation.id, pinned: !conversation.pinned })}
          className="shrink-0 rounded-md p-1 outline-none hover:bg-background focus-visible:ring-3 focus-visible:ring-ring/50"
        >
          {conversation.pinned ? <PinOff className="h-4 w-4" /> : <Pin className="h-4 w-4" />}
        </button>
        <DropdownMenu>
          <DropdownMenuTrigger
            aria-label={`Options for ${conversation.title}`}
            className="mr-1 shrink-0 rounded-md p-1 outline-none hover:bg-background focus-visible:ring-3 focus-visible:ring-ring/50"
          >
            <Ellipsis className="h-4 w-4" />
          </DropdownMenuTrigger>
          <DropdownMenuContent>
            <DropdownMenuItem onClick={() => setRenameTarget(conversation)}>
              Rename
            </DropdownMenuItem>
            <DropdownMenuItem
              className="text-destructive data-[highlighted]:bg-destructive/10 data-[highlighted]:text-destructive"
              onClick={() => setDeleteTarget(conversation)}
            >
              Delete
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    );
  }

  return (
    <>
      <Accordion defaultValue={['pinned', 'conversations']}>
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

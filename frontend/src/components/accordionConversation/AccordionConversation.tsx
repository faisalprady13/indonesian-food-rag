import { Ellipsis } from 'lucide-react';
import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
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
import { getConversations } from '@/lib/api';
import { cn } from '@/lib/utils';
import type { Conversation } from '@/types/Chat.ts';
import ConversationListSkeleton from '@/components/skeletons/ConversationListSkeleton.tsx';

const triggerClass = 'px-1 text-xs font-semibold tracking-wide text-muted-foreground uppercase';

type AccordionConversationProps = {
  enabled?: boolean;
  onNavigate?: () => void;
  className?: string;
};

export default function AccordionConversation({
  enabled = true,
  onNavigate,
  className,
}: Readonly<AccordionConversationProps>) {
  const navigate = useNavigate();
  const { conversationId } = useParams<{ conversationId: string }>();

  const { data: conversations, isLoading } = useQuery({
    queryKey: ['chat', 'conversations'],
    queryFn: ({ signal }) => getConversations(signal),
    enabled,
  });

  const [renameTarget, setRenameTarget] = useState<Conversation | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Conversation | null>(null);

  function handleSelectConversation(id: number) {
    navigate(`/chat/${id}`);
    onNavigate?.();
  }

  return (
    <>
      <Accordion defaultValue={['conversations']}>
        <AccordionItem value="conversations">
          <AccordionTrigger className={cn(triggerClass, className)}>Conversations</AccordionTrigger>
          <AccordionPanel>
            <div className="flex flex-col gap-1">
              {isLoading ? (
                <ConversationListSkeleton />
              ) : conversations && conversations.length > 0 ? (
                conversations.map((conversation) => {
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
                })
              ) : (
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

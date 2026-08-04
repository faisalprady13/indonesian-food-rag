import { Ellipsis, Pin, PinOff } from 'lucide-react';
import { useState } from 'react';
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
} from '@/components/ui/dropdown-menu.tsx';
import { cn } from '@/lib/utils';
import type { Conversation } from '@/types/Chat.ts';

type ConversationRowProps = {
  conversation: Conversation;
  isActive: boolean;
  onSelect: (id: number) => void;
  onTogglePinned: (conversation: Conversation) => void;
  onRename: (conversation: Conversation) => void;
  onDelete: (conversation: Conversation) => void;
};

export default function ConversationRow({
  conversation,
  isActive,
  onSelect,
  onTogglePinned,
  onRename,
  onDelete,
}: Readonly<ConversationRowProps>) {
  const [menuOpen, setMenuOpen] = useState(false);

  const revealClass = menuOpen
    ? 'opacity-100'
    : 'opacity-0 group-hover:opacity-100 group-focus-within:opacity-100 focus-visible:opacity-100';

  return (
    <div
      className={`group flex items-center rounded-md text-sm transition-colors ${
        isActive
          ? 'bg-muted text-foreground'
          : 'text-muted-foreground hover:bg-muted hover:text-foreground'
      }`}
    >
      <button
        type="button"
        onClick={() => onSelect(conversation.id)}
        className="min-w-0 flex-1 truncate px-2.5 py-1.5 text-left"
      >
        {conversation.title}
      </button>
      <button
        type="button"
        aria-label={conversation.pinned ? `Unpin ${conversation.title}` : `Pin ${conversation.title}`}
        onClick={() => onTogglePinned(conversation)}
        className={cn(
          'shrink-0 rounded-md p-1 outline-none hover:bg-background focus-visible:ring-3 focus-visible:ring-ring/50',
          revealClass,
        )}
      >
        {conversation.pinned ? <PinOff className="h-4 w-4" /> : <Pin className="h-4 w-4" />}
      </button>
      <DropdownMenu open={menuOpen} onOpenChange={setMenuOpen}>
        <DropdownMenuTrigger
          aria-label={`Options for ${conversation.title}`}
          className={cn(
            'mr-1 shrink-0 rounded-md p-1 outline-none hover:bg-background focus-visible:ring-3 focus-visible:ring-ring/50',
            revealClass,
          )}
        >
          <Ellipsis className="h-4 w-4" />
        </DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuItem onClick={() => onRename(conversation)}>Rename</DropdownMenuItem>
          <DropdownMenuItem
            className="text-destructive data-[highlighted]:bg-destructive/10 data-[highlighted]:text-destructive"
            onClick={() => onDelete(conversation)}
          >
            Delete
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  );
}
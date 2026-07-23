import { Bubble, BubbleContent } from '@/components/ui/bubble.tsx';
import type { Message } from '@/types/Chat.ts';

interface BubbleMessageProps {
  message: Message;
}

export default function BubbleMessage({ message }: BubbleMessageProps) {
  return (
    <Bubble
      variant={message.role === 'USER' ? 'default' : 'muted'}
      align={message.role === 'USER' ? 'end' : 'start'}
      className="text-left"
    >
      <BubbleContent>{message.content}</BubbleContent>
    </Bubble>
  );
}

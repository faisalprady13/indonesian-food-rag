import { Loader2 } from 'lucide-react';
import { Bubble, BubbleContent } from '@/components/ui/bubble.tsx';

export default function BubbleSpinner() {
  return (
    <Bubble variant="muted" align="start" aria-label="Waiting for reply">
      <BubbleContent className="flex items-center">
        <Loader2 size={16} className="animate-spin" />
      </BubbleContent>
    </Bubble>
  );
}

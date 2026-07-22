import { Bubble } from '@/components/ui/bubble.tsx';
import { Skeleton } from '@/components/ui/skeleton.tsx';

const BUBBLES: { align: 'start' | 'end'; width: string }[] = [
  { align: 'start', width: 'w-56' },
  { align: 'end', width: 'w-40' },
  { align: 'start', width: 'w-64' },
];

export default function ChatSkeleton() {
  return (
    <>
      {BUBBLES.map(({ align, width }, index) => (
        <Bubble key={index} variant="ghost" align={align}>
          <Skeleton className={`h-9 rounded-2xl ${width}`} />
        </Bubble>
      ))}
    </>
  );
}

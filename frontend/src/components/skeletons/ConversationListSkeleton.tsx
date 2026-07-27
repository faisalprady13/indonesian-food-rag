import { Skeleton } from '@/components/ui/skeleton.tsx';

const WIDTHS = ['w-3/4', 'w-1/2', 'w-2/3'];

export default function ConversationListSkeleton() {
  return (
    <>
      {WIDTHS.map((width, index) => (
        <Skeleton key={index} className={`ml-2.5 py-1 my-1 h-5 ${width}`} />
      ))}
    </>
  );
}

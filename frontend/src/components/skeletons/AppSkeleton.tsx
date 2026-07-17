import { Card } from '@/components/ui/card.tsx';
import { Skeleton } from '@/components/ui/skeleton.tsx';

export default function AppSkeleton() {
  return (
    <div className="flex min-h-svh w-full flex-col">
      <header className="flex items-center justify-between border-b border-border px-4 py-3 sm:px-6">
        <div className="flex items-center gap-6">
          <Skeleton className="h-8 w-8 rounded-md" />
          <Skeleton className="hidden h-8 w-20 rounded-md sm:block" />
        </div>
        <div className="flex items-center gap-2">
          <Skeleton className="size-9 rounded-full" />
          <Skeleton className="hidden h-4 w-16 sm:block" />
        </div>
      </header>

      <main className="flex flex-1 flex-col p-6 md:p-10">
        <div className="mx-auto flex w-full max-w-4xl flex-1 flex-col gap-6">
          <Skeleton className="mx-auto h-9 w-full max-w-md rounded-md" />

          <div className="flex h-9 w-fit gap-1 rounded-lg bg-muted p-[3px]">
            <Skeleton className="h-full w-16 rounded-md" />
            <Skeleton className="h-full w-32 rounded-md" />
          </div>

          <Card className="w-full"></Card>
        </div>
      </main>
    </div>
  );
}

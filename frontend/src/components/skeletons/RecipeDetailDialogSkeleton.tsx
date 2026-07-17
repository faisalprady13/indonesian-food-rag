import {
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog.tsx';
import { Skeleton } from '@/components/ui/skeleton.tsx';

export default function RecipeDetailDialogSkeleton() {
  return (
    <DialogContent>
      <DialogHeader>
        <div className="flex items-center gap-2">
          <DialogTitle>
            <Skeleton className="h-5 w-40" />
          </DialogTitle>
          <Skeleton className="h-4 w-4 shrink-0" />
        </div>
        <DialogDescription>
          <Skeleton className="h-3 w-28" />
        </DialogDescription>
      </DialogHeader>
      <div className="flex flex-col gap-4">
        <div>
          <h3 className="mb-1.5 text-sm font-medium">Ingredients</h3>
          <div className="flex flex-col gap-2">
            <Skeleton className="h-4 w-40" />
            <Skeleton className="h-4 w-32" />
            <Skeleton className="h-4 w-36" />
          </div>
        </div>
        <div>
          <h3 className="mb-1.5 text-sm font-medium">Steps</h3>
          <div className="flex flex-col gap-2">
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-2/3" />
          </div>
        </div>
      </div>
    </DialogContent>
  );
}

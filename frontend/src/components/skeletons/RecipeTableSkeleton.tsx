import { TableCell, TableRow } from '@/components/ui/table.tsx';
import { Skeleton } from '@/components/ui/skeleton.tsx';

interface RecipeTableSkeletonProps {
  rows: number;
}

export default function RecipeTableSkeleton({ rows }: RecipeTableSkeletonProps) {
  return (
    <>
      {Array.from({ length: rows }, (_, index) => (
        <TableRow key={index}>
          <TableCell>
            <Skeleton className="h-5 w-5 rounded-full" />
          </TableCell>
          <TableCell>
            <Skeleton className="h-5 w-32" />
          </TableCell>
          <TableCell>
            <Skeleton className="h-5 w-20" />
          </TableCell>
        </TableRow>
      ))}
    </>
  );
}

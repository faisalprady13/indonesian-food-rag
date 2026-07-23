import { cn } from '@/lib/utils';

function Skeleton({
  className,
  as: Component = 'div',
  ...props
}: React.ComponentProps<'div'> & {
  as?: React.ElementType;
}) {
  return (
    <Component
      data-slot="skeleton"
      className={cn('animate-pulse rounded-md bg-muted', className)}
      {...props}
    />
  );
}

export { Skeleton };

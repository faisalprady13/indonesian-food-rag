import * as React from 'react';
import { useRender } from '@base-ui/react/use-render';
import { cva, type VariantProps } from 'class-variance-authority';

import { cn } from '@/lib/utils';

const bubbleVariants = cva('w-fit max-w-[80%] rounded-2xl px-4 py-2 text-sm', {
  variants: {
    variant: {
      default: 'bg-primary text-primary-foreground',
      secondary: 'bg-secondary text-secondary-foreground',
      muted: 'bg-muted text-foreground',
      tinted: 'bg-primary/10 text-foreground',
      outline: 'border border-border bg-transparent text-foreground',
      ghost: 'bg-transparent px-0 py-0 text-foreground',
      destructive: 'bg-destructive/10 text-destructive',
    },
    align: {
      start: 'self-start',
      end: 'self-end',
    },
  },
  defaultVariants: {
    variant: 'default',
    align: 'start',
  },
});

interface BubbleProps extends React.ComponentProps<'div'>, VariantProps<typeof bubbleVariants> {}

function Bubble({ className, variant, align, ...props }: BubbleProps) {
  return (
    <div
      data-slot="bubble"
      className={cn(
        'group/bubble relative flex flex-col gap-2',
        bubbleVariants({ variant, align }),
        className,
      )}
      {...props}
    />
  );
}

function BubbleContent({ className, render = <div />, ...props }: useRender.ComponentProps<'div'>) {
  return useRender({
    render,
    props: {
      'data-slot': 'bubble-content',
      className: cn('whitespace-pre-line', className),
      ...props,
    },
  });
}

function BubbleReactions({
  className,
  side = 'bottom',
  align = 'end',
  ...props
}: React.ComponentProps<'div'> & { side?: 'top' | 'bottom'; align?: 'start' | 'end' }) {
  return (
    <div
      data-slot="bubble-reactions"
      className={cn(
        'absolute flex items-center gap-1 opacity-0 transition-opacity group-hover/bubble:opacity-100',
        side === 'top' ? 'bottom-full mb-1' : 'top-full mt-1',
        align === 'end' ? 'right-0' : 'left-0',
        className,
      )}
      {...props}
    />
  );
}

function BubbleGroup({ className, ...props }: React.ComponentProps<'div'>) {
  return (
    <div data-slot="bubble-group" className={cn('flex flex-col gap-0.5', className)} {...props} />
  );
}

export { Bubble, BubbleContent, BubbleReactions, BubbleGroup };

import * as React from 'react';
import { NumberField as NumberFieldPrimitive } from '@base-ui/react/number-field';
import { Minus, Plus } from 'lucide-react';

import { cn } from '@/lib/utils';

function NumberField(props: React.ComponentProps<typeof NumberFieldPrimitive.Root>) {
  return <NumberFieldPrimitive.Root data-slot="number-field" {...props} />;
}

function NumberFieldGroup({
  className,
  ...props
}: React.ComponentProps<typeof NumberFieldPrimitive.Group>) {
  return (
    <NumberFieldPrimitive.Group
      data-slot="number-field-group"
      className={cn(
        'flex h-9 w-full items-center overflow-hidden rounded-md border border-input bg-transparent shadow-xs transition-[color,box-shadow] has-focus-visible:border-ring has-focus-visible:ring-3 has-focus-visible:ring-ring/50 dark:bg-input/30',
        className,
      )}
      {...props}
    />
  );
}

function NumberFieldDecrement({
  className,
  ...props
}: React.ComponentProps<typeof NumberFieldPrimitive.Decrement>) {
  return (
    <NumberFieldPrimitive.Decrement
      data-slot="number-field-decrement"
      className={cn(
        'flex h-full w-9 shrink-0 items-center justify-center text-muted-foreground outline-none transition-colors hover:bg-muted hover:text-foreground disabled:pointer-events-none disabled:opacity-50',
        className,
      )}
      {...props}
    >
      <Minus size={14} />
    </NumberFieldPrimitive.Decrement>
  );
}

function NumberFieldIncrement({
  className,
  ...props
}: React.ComponentProps<typeof NumberFieldPrimitive.Increment>) {
  return (
    <NumberFieldPrimitive.Increment
      data-slot="number-field-increment"
      className={cn(
        'flex h-full w-9 shrink-0 items-center justify-center text-muted-foreground outline-none transition-colors hover:bg-muted hover:text-foreground disabled:pointer-events-none disabled:opacity-50',
        className,
      )}
      {...props}
    >
      <Plus size={14} />
    </NumberFieldPrimitive.Increment>
  );
}

function NumberFieldInput({
  className,
  ...props
}: React.ComponentProps<typeof NumberFieldPrimitive.Input>) {
  return (
    <NumberFieldPrimitive.Input
      data-slot="number-field-input"
      className={cn(
        'h-full min-w-0 flex-1 border-x border-input bg-transparent px-2 text-center text-sm outline-none',
        className,
      )}
      {...props}
    />
  );
}

export {
  NumberField,
  NumberFieldGroup,
  NumberFieldDecrement,
  NumberFieldIncrement,
  NumberFieldInput,
};

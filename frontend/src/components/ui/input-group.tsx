import * as React from 'react';
import { Input as InputPrimitive } from '@base-ui/react/input';
import { cva, type VariantProps } from 'class-variance-authority';

import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button.tsx';

function InputGroup({ className, ...props }: React.ComponentProps<'div'>) {
  return (
    <div
      data-slot="input-group"
      role="group"
      className={cn(
        'group/input-group relative flex w-full items-center rounded-md border border-input bg-transparent shadow-xs transition-[color,box-shadow] has-[[data-slot=input-group-control]:focus-visible]:border-ring has-[[data-slot=input-group-control]:focus-visible]:ring-3 has-[[data-slot=input-group-control]:focus-visible]:ring-ring/50 has-[textarea]:h-auto has-[textarea]:flex-col dark:bg-input/30',
        className,
      )}
      {...props}
    />
  );
}

const inputGroupAddonVariants = cva(
  'flex items-center justify-center gap-1.5 py-1.5 text-muted-foreground',
  {
    variants: {
      align: {
        'inline-start': 'order-first pl-2.5',
        'inline-end': 'order-last pr-2.5',
        'block-start': 'order-first w-full justify-start px-2.5 pt-2.5',
        'block-end': 'order-last w-full justify-start px-2.5 pb-2.5',
      },
    },
    defaultVariants: {
      align: 'inline-start',
    },
  },
);

function InputGroupAddon({
  className,
  align = 'inline-start',
  ...props
}: React.ComponentProps<'div'> & VariantProps<typeof inputGroupAddonVariants>) {
  return (
    <div
      data-slot="input-group-addon"
      className={cn(inputGroupAddonVariants({ align }), className)}
      {...props}
    />
  );
}

function InputGroupButton({
  className,
  type = 'button',
  variant = 'ghost',
  size = 'xs',
  ...props
}: React.ComponentProps<typeof Button>) {
  return (
    <Button
      type={type}
      variant={variant}
      size={size}
      data-slot="input-group-button"
      className={cn('shadow-none', className)}
      {...props}
    />
  );
}

function InputGroupText({ className, ...props }: React.ComponentProps<'span'>) {
  return (
    <span
      data-slot="input-group-text"
      className={cn('flex items-center gap-1.5 text-sm text-muted-foreground', className)}
      {...props}
    />
  );
}

function InputGroupInput({ className, ...props }: React.ComponentProps<typeof InputPrimitive>) {
  return (
    <InputPrimitive
      data-slot="input-group-control"
      className={cn(
        'h-9 w-full min-w-0 flex-1 bg-transparent px-2.5 py-1 text-base outline-none placeholder:text-muted-foreground disabled:pointer-events-none disabled:cursor-not-allowed disabled:opacity-50 md:text-sm',
        className,
      )}
      {...props}
    />
  );
}

function InputGroupTextarea({ className, ...props }: React.ComponentProps<'textarea'>) {
  return (
    <textarea
      data-slot="input-group-control"
      className={cn(
        'min-h-16 w-full flex-1 resize-none bg-transparent px-2.5 py-2 text-base outline-none placeholder:text-muted-foreground disabled:pointer-events-none disabled:cursor-not-allowed disabled:opacity-50 md:text-sm',
        className,
      )}
      {...props}
    />
  );
}

export {
  InputGroup,
  InputGroupAddon,
  InputGroupButton,
  InputGroupText,
  InputGroupInput,
  InputGroupTextarea,
};

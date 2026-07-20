import * as React from 'react';
import { Select as SelectPrimitive } from '@base-ui/react/select';
import { Check, ChevronsUpDown } from 'lucide-react';

import { cn } from '@/lib/utils';

function Select<Value, Multiple extends boolean | undefined = false>(
  props: SelectPrimitive.Root.Props<Value, Multiple>,
) {
  return <SelectPrimitive.Root data-slot="select" {...props} />;
}

function SelectValue(props: React.ComponentProps<typeof SelectPrimitive.Value>) {
  return <SelectPrimitive.Value data-slot="select-value" {...props} />;
}

function SelectTrigger({
  className,
  children,
  ...props
}: React.ComponentProps<typeof SelectPrimitive.Trigger>) {
  return (
    <SelectPrimitive.Trigger
      data-slot="select-trigger"
      className={cn(
        'flex h-9 w-full items-center justify-between gap-2 rounded-md border border-input bg-transparent px-2.5 py-1 text-sm shadow-xs outline-none transition-[color,box-shadow] focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 disabled:pointer-events-none disabled:cursor-not-allowed disabled:opacity-50 dark:bg-input/30',
        className,
      )}
      {...props}
    >
      {children}
      <SelectPrimitive.Icon className="text-muted-foreground">
        <ChevronsUpDown size={16} />
      </SelectPrimitive.Icon>
    </SelectPrimitive.Trigger>
  );
}

function SelectContent({
  className,
  children,
  sideOffset = 8,
  align = 'start',
  ...props
}: React.ComponentProps<typeof SelectPrimitive.Popup> &
  Pick<React.ComponentProps<typeof SelectPrimitive.Positioner>, 'sideOffset' | 'align' | 'side'>) {
  return (
    <SelectPrimitive.Portal>
      <SelectPrimitive.Positioner
        data-slot="select-positioner"
        sideOffset={sideOffset}
        align={align}
        className="z-50"
      >
        <SelectPrimitive.Popup
          data-slot="select-content"
          className={cn(
            'max-h-[min(24rem,var(--available-height))] min-w-[var(--anchor-width)] origin-[var(--transform-origin)] overflow-y-auto rounded-lg bg-card p-1 text-card-foreground shadow-lg ring-1 ring-foreground/10 outline-none data-[ending-style]:scale-95 data-[ending-style]:opacity-0 data-[starting-style]:scale-95 data-[starting-style]:opacity-0',
            className,
          )}
          {...props}
        >
          <SelectPrimitive.List data-slot="select-list">{children}</SelectPrimitive.List>
        </SelectPrimitive.Popup>
      </SelectPrimitive.Positioner>
    </SelectPrimitive.Portal>
  );
}

function SelectItem({
  className,
  children,
  ...props
}: React.ComponentProps<typeof SelectPrimitive.Item>) {
  return (
    <SelectPrimitive.Item
      data-slot="select-item"
      className={cn(
        'relative flex cursor-pointer items-center gap-2 rounded-md py-1.5 pr-2.5 pl-8 text-sm font-medium text-muted-foreground outline-none select-none data-[highlighted]:bg-muted data-[highlighted]:text-foreground',
        className,
      )}
      {...props}
    >
      <SelectPrimitive.ItemIndicator className="absolute left-2.5 flex items-center">
        <Check size={14} />
      </SelectPrimitive.ItemIndicator>
      <SelectPrimitive.ItemText>{children}</SelectPrimitive.ItemText>
    </SelectPrimitive.Item>
  );
}

export { Select, SelectValue, SelectTrigger, SelectContent, SelectItem };

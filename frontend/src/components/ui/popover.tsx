import * as React from 'react';
import { Popover as PopoverPrimitive } from '@base-ui/react/popover';

import { cn } from '@/lib/utils';

function Popover(props: React.ComponentProps<typeof PopoverPrimitive.Root>) {
  return <PopoverPrimitive.Root data-slot="popover" {...props} />;
}

function PopoverTrigger(props: React.ComponentProps<typeof PopoverPrimitive.Trigger>) {
  return <PopoverPrimitive.Trigger data-slot="popover-trigger" {...props} />;
}

function PopoverPortal(props: React.ComponentProps<typeof PopoverPrimitive.Portal>) {
  return <PopoverPrimitive.Portal data-slot="popover-portal" {...props} />;
}

function PopoverContent({
  className,
  sideOffset = 8,
  align = 'end',
  ...props
}: React.ComponentProps<typeof PopoverPrimitive.Popup> &
  Pick<React.ComponentProps<typeof PopoverPrimitive.Positioner>, 'sideOffset' | 'align' | 'side'>) {
  return (
    <PopoverPortal>
      <PopoverPrimitive.Positioner
        data-slot="popover-positioner"
        sideOffset={sideOffset}
        align={align}
        className="z-50"
      >
        <PopoverPrimitive.Popup
          data-slot="popover-content"
          className={cn(
            'min-w-40 origin-[var(--transform-origin)] rounded-lg bg-card p-3 text-card-foreground shadow-lg ring-1 ring-foreground/10 outline-none data-[ending-style]:scale-95 data-[ending-style]:opacity-0 data-[starting-style]:scale-95 data-[starting-style]:opacity-0',
            className,
          )}
          {...props}
        />
      </PopoverPrimitive.Positioner>
    </PopoverPortal>
  );
}

export { Popover, PopoverTrigger, PopoverContent };

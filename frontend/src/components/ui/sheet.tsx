import * as React from 'react';
import { Dialog as DialogPrimitive } from '@base-ui/react/dialog';
import { HugeiconsIcon } from '@hugeicons/react';
import { Cancel01Icon } from '@hugeicons/core-free-icons';

import { cn } from '@/lib/utils';

function Sheet(props: React.ComponentProps<typeof DialogPrimitive.Root>) {
  return <DialogPrimitive.Root data-slot="sheet" {...props} />;
}

function SheetTrigger(props: React.ComponentProps<typeof DialogPrimitive.Trigger>) {
  return <DialogPrimitive.Trigger data-slot="sheet-trigger" {...props} />;
}

function SheetClose(props: React.ComponentProps<typeof DialogPrimitive.Close>) {
  return <DialogPrimitive.Close data-slot="sheet-close" {...props} />;
}

function SheetPortal(props: React.ComponentProps<typeof DialogPrimitive.Portal>) {
  return <DialogPrimitive.Portal data-slot="sheet-portal" {...props} />;
}

function SheetBackdrop({
  className,
  ...props
}: React.ComponentProps<typeof DialogPrimitive.Backdrop>) {
  return (
    <DialogPrimitive.Backdrop
      data-slot="sheet-backdrop"
      className={cn(
        'fixed inset-0 z-50 bg-black/50 data-[ending-style]:opacity-0 data-[starting-style]:opacity-0',
        className,
      )}
      {...props}
    />
  );
}

type SheetSide = 'left' | 'right';

function SheetContent({
  className,
  children,
  side = 'left',
  showCloseButton = true,
  ...props
}: React.ComponentProps<typeof DialogPrimitive.Popup> & {
  side?: SheetSide;
  showCloseButton?: boolean;
}) {
  return (
    <SheetPortal>
      <SheetBackdrop />
      <DialogPrimitive.Popup
        data-slot="sheet-content"
        className={cn(
          'fixed inset-y-0 z-50 flex h-full w-3/4 max-w-xs flex-col gap-4 bg-card p-6 text-card-foreground shadow-lg outline-none transition-transform duration-300 ease-out',
          side === 'left' &&
            'left-0 border-r border-border data-[ending-style]:-translate-x-full data-[starting-style]:-translate-x-full',
          side === 'right' &&
            'right-0 border-l border-border data-[ending-style]:translate-x-full data-[starting-style]:translate-x-full',
          className,
        )}
        {...props}
      >
        {children}
        {showCloseButton && (
          <SheetClose className="absolute top-4 right-4 rounded-md text-muted-foreground opacity-70 outline-none transition-opacity hover:opacity-100 focus-visible:ring-3 focus-visible:ring-ring/50">
            <HugeiconsIcon icon={Cancel01Icon} size={16} />
            <span className="sr-only">Close</span>
          </SheetClose>
        )}
      </DialogPrimitive.Popup>
    </SheetPortal>
  );
}

function SheetTitle({ className, ...props }: React.ComponentProps<typeof DialogPrimitive.Title>) {
  return (
    <DialogPrimitive.Title
      data-slot="sheet-title"
      className={cn('font-heading text-base leading-normal font-medium', className)}
      {...props}
    />
  );
}

export { Sheet, SheetTrigger, SheetClose, SheetContent, SheetTitle };

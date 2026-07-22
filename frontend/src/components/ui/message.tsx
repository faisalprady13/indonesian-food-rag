import * as React from 'react';

import { cn } from '@/lib/utils';

type MessageAlign = 'start' | 'end';

const MessageAlignContext = React.createContext<MessageAlign>('start');

function Message({
  align = 'start',
  className,
  ...props
}: React.ComponentProps<'div'> & { align?: MessageAlign }) {
  return (
    <MessageAlignContext.Provider value={align}>
      <div
        data-slot="message"
        data-align={align}
        className={cn(
          'flex w-full items-end gap-2',
          align === 'end' ? 'flex-row-reverse' : 'flex-row',
          className,
        )}
        {...props}
      />
    </MessageAlignContext.Provider>
  );
}

function MessageAvatar({ className, ...props }: React.ComponentProps<'div'>) {
  return (
    <div data-slot="message-avatar" className={cn('shrink-0 self-end', className)} {...props} />
  );
}

function MessageContent({ className, ...props }: React.ComponentProps<'div'>) {
  const align = React.useContext(MessageAlignContext);
  return (
    <div
      data-slot="message-content"
      className={cn(
        'flex max-w-[80%] flex-col gap-1',
        align === 'end' ? 'items-end' : 'items-start',
        className,
      )}
      {...props}
    />
  );
}

function MessageHeader({ className, ...props }: React.ComponentProps<'div'>) {
  return (
    <div
      data-slot="message-header"
      className={cn(
        'flex items-center gap-1.5 self-start text-xs text-muted-foreground',
        className,
      )}
      {...props}
    />
  );
}

function MessageFooter({ className, ...props }: React.ComponentProps<'div'>) {
  const align = React.useContext(MessageAlignContext);
  return (
    <div
      data-slot="message-footer"
      className={cn(
        'flex items-center gap-1.5 text-xs text-muted-foreground',
        align === 'end' ? 'self-end' : 'self-start',
        className,
      )}
      {...props}
    />
  );
}

function MessageGroup({ className, ...props }: React.ComponentProps<'div'>) {
  return (
    <div data-slot="message-group" className={cn('flex flex-col gap-1', className)} {...props} />
  );
}

export { Message, MessageAvatar, MessageContent, MessageHeader, MessageFooter, MessageGroup };

import type { RefObject } from 'react';
import { HugeiconsIcon } from '@hugeicons/react';
import { SentIcon } from '@hugeicons/core-free-icons';
import { Loader2 } from 'lucide-react';
import {
  InputGroup,
  InputGroupAddon,
  InputGroupButton,
  InputGroupInput,
} from '@/components/ui/input-group.tsx';

interface MessageInputGroupProps {
  inputRef: RefObject<HTMLInputElement | null>;
  onSubmit: () => void;
  sending: boolean;
}

export default function MessageInputGroup({ inputRef, onSubmit, sending }: MessageInputGroupProps) {
  return (
    <>
      <div className="fixed bottom-0 left-0 w-full h-15 bg-black/80"></div>
      <div className={'MessageInputGroup fixed pb-8 bottom-0 left-0 w-full px-6'}>
        <form
          onSubmit={(e) => {
            e.preventDefault();
            onSubmit();
          }}
        >
          <InputGroup className={'w-full max-w-3xl m-auto dark:bg-muted rounded-full'}>
            <InputGroupInput
              ref={inputRef}
              placeholder="Type a message..."
              autoComplete="off"
              disabled={sending}
              className="w-full"
            />
            <InputGroupAddon align="inline-end">
              <InputGroupButton
                type="submit"
                size="icon-lg"
                variant="default"
                disabled={sending}
                aria-label="Send message"
                className="rounded-full"
              >
                {sending ? (
                  <Loader2 size={20} className="animate-spin" />
                ) : (
                  <HugeiconsIcon icon={SentIcon} size={20} />
                )}
              </InputGroupButton>
            </InputGroupAddon>
          </InputGroup>
        </form>
      </div>
    </>
  );
}

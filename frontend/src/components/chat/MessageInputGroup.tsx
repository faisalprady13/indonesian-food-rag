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
  value: string;
  onChange: (value: string) => void;
  onSubmit: () => void;
  sending: boolean;
}

export default function MessageInputGroup({
  value,
  onChange,
  onSubmit,
  sending,
}: MessageInputGroupProps) {
  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        onSubmit();
      }}
      className="pt-4"
    >
      <InputGroup>
        <InputGroupInput
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder="Type a message..."
          autoComplete="off"
          disabled={sending}
        />
        <InputGroupAddon align="inline-end">
          <InputGroupButton
            type="submit"
            size="icon-sm"
            variant="default"
            disabled={!value.trim() || sending}
            aria-label="Send message"
          >
            {sending ? (
              <Loader2 size={16} className="animate-spin" />
            ) : (
              <HugeiconsIcon icon={SentIcon} size={16} />
            )}
          </InputGroupButton>
        </InputGroupAddon>
      </InputGroup>
    </form>
  );
}

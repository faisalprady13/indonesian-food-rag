import type {RefObject} from 'react';
import {HugeiconsIcon} from '@hugeicons/react';
import {ArrowUp} from 'lucide-react';
import {Loader2} from 'lucide-react';
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

export default function MessageInputGroup({inputRef, onSubmit, sending}: MessageInputGroupProps) {
    return (
        <>
            <div className="fixed bottom-0 left-0 w-full h-15 bg-background/60 backdrop-blur-sm"/>
            <div className={'MessageInputGroup fixed pb-8 bottom-0 left-0 w-full px-6'}>
                <form
                    onSubmit={(e) => {
                        e.preventDefault();
                        onSubmit();
                    }}
                >
                    <InputGroup className={'w-full max-w-3xl m-auto dark:bg-muted rounded-full pl-2'}>
                        <InputGroupInput
                            ref={inputRef}
                            placeholder="Ask anything about recipe"
                            autoComplete="off"
                            disabled={sending}
                            className="w-full"
                        />
                        <InputGroupAddon align="inline-end" className={"pr-2"}>
                            <InputGroupButton
                                type="submit"
                                size="icon-lg"
                                variant="default"
                                disabled={sending}
                                aria-label="Send message"
                                className="rounded-full"
                            >
                                {sending ? (
                                    <Loader2 className="size-6 animate-spin"/>
                                ) : (
                                    <ArrowUp className="size-5"/>
                                )}
                            </InputGroupButton>
                        </InputGroupAddon>
                    </InputGroup>
                </form>
            </div>
        </>
    );
}

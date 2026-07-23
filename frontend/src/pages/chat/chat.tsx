/* eslint-disable react-hooks/set-state-in-effect */
import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useMutation, useQuery } from '@tanstack/react-query';
import BubbleMessage from '@/components/chat/BubbleMessage.tsx';
import BubbleSpinner from '@/components/chat/BubbleSpinner.tsx';
import MessageInputGroup from '@/components/chat/MessageInputGroup.tsx';
import ChatSkeleton from '@/components/skeletons/ChatSkeleton.tsx';
import type { Message as ChatMessage } from '@/types/Chat.ts';
import { getDetailConversation, sendMessage } from '@/lib/api';

export default function Chat() {
  const { conversationId: conversationIdParam } = useParams<{ conversationId: string }>();
  const conversationId = conversationIdParam ? Number(conversationIdParam) : null;

  const { data: seedMessages, isLoading: loading } = useQuery({
    queryKey: ['chat', 'messages', conversationId],
    queryFn: ({ signal }) => getDetailConversation(conversationId as number, signal),
    enabled: conversationId !== null,
  });
  //TODO: might not be working
  const [messages, setMessages] = useState<ChatMessage[]>(seedMessages?.messages ?? []);
  const inputRef = useRef<HTMLInputElement>(null);
  const bottomRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();

  const sendMutation = useMutation({
    mutationFn: ({ content }: { content: string }) => sendMessage({ conversationId, content }),
    onSuccess: (reply) => {
      navigate(`/chat/${reply.conversationId}`, { replace: true });
      setMessages((prev) => [...prev, reply]);
    },
    onError: (error) => {
      console.error(error);
    },
  });
  const sending = sendMutation.isPending;

  useEffect(() => {
    if (conversationId === null) {
      setMessages([]);
    }
  }, [conversationId]);

  useEffect(() => {
    if (conversationId !== null && seedMessages) {
      setMessages(seedMessages.messages);
    }
  }, [seedMessages, conversationId]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, sending]);

  function handleSend() {
    const content = inputRef.current?.value.trim() ?? '';
    if (!content || sending) {
      return;
    }

    setMessages((prev) => [
      ...prev,
      {
        id: (prev.at(-1)?.id ?? 0) + 1,
        conversationId: conversationId,
        role: 'USER',
        content,
        createdAt: new Date().toISOString(),
      },
    ]);
    if (inputRef.current) {
      inputRef.current.value = '';
    }

    sendMutation.mutate({ content });
  }
  return (
    <div className="mx-auto flex min-h-0 w-full max-w-3xl flex-1 flex-col">
      <div className="flex min-h-0 flex-1 flex-col gap-3 overflow-y-auto py-4">
        {loading ? (
          <ChatSkeleton />
        ) : messages.length === 0 && !sending ? (
          <p className="text-sm text-muted-foreground">Start the conversation below.</p>
        ) : (
          <>
            {messages.map((message, i) => (
              <BubbleMessage key={i} message={message} />
            ))}
            {sending && <BubbleSpinner />}
          </>
        )}
        <div ref={bottomRef} />
      </div>

      <MessageInputGroup inputRef={inputRef} onSubmit={handleSend} sending={sending} />
    </div>
  );
}

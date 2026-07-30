import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import BubbleMessage from '@/components/chat/BubbleMessage.tsx';
import BubbleSpinner from '@/components/chat/BubbleSpinner.tsx';
import MessageInputGroup from '@/components/chat/MessageInputGroup.tsx';
import ChatSkeleton from '@/components/skeletons/ChatSkeleton.tsx';
import type { Conversation, Message as ChatMessage } from '@/types/Chat.ts';
import { getDetailConversation, sendMessage } from '@/queries';

export default function Chat() {
  const { conversationId: conversationIdParam } = useParams<{ conversationId: string }>();
  const conversationId = conversationIdParam ? Number(conversationIdParam) : null;

  const { data: seedMessages, isLoading: loading } = useQuery({
    queryKey: ['chat', 'messages', conversationId],
    queryFn: ({ signal }) => getDetailConversation(conversationId as number, signal),
    enabled: conversationId !== null,
  });
  const [messages, setMessages] = useState<ChatMessage[]>(seedMessages?.messages ?? []);
  // Tracks the previous conversationId/seedMessages seen so `messages` can be
  // reset/synced during render instead of via a setState-in-effect round trip.
  const [prevConversationId, setPrevConversationId] = useState(conversationId);
  const [prevSeedMessages, setPrevSeedMessages] = useState(seedMessages);
  const inputRef = useRef<HTMLInputElement>(null);
  const bottomRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const sendMutation = useMutation({
    mutationFn: ({ content }: { content: string }) => sendMessage({ conversationId, content }),
    onSuccess: (reply) => {
      const updatedMessages = [...messages, reply];
      // Prime the cache for the (possibly brand-new) conversationId with what we
      // already have locally, so the query triggered by navigating below finds
      // fresh data immediately instead of refetching and re-flashing the list.
      queryClient.setQueryData<Conversation>(['chat', 'messages', reply.conversationId], (old) => ({
        id: reply.conversationId as number,
        title: old?.title ?? '',
        createdAt: old?.createdAt ?? reply.createdAt,
        updatedAt: reply.createdAt,
        messages: updatedMessages,
      }));
      setMessages(updatedMessages);
      navigate(`/chat/${reply.conversationId}`, { replace: true });
    },
    onError: (error) => {
      console.error(error);
    },
  });
  const sending = sendMutation.isPending;

  if (conversationId !== prevConversationId) {
    setPrevConversationId(conversationId);
    if (conversationId === null) {
      setMessages([]);
    }
  }

  if (seedMessages !== prevSeedMessages) {
    setPrevSeedMessages(seedMessages);
    if (conversationId !== null && seedMessages) {
      setMessages(seedMessages.messages);
    }
  }

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
    <div className="mx-auto flex min-h-0 w-full max-w-3xl flex-1 flex-col p-8">
      <div className="flex min-h-0 flex-1 flex-col gap-3 overflow-y-auto py-4 my-18">
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

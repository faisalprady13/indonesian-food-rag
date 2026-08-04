import { http } from '@/queries/http.ts';
import type { Conversation, Message, MessageRequest } from '@/types/Chat.ts';

export async function sendMessage(messageRequest: MessageRequest): Promise<Message> {
  const { data } = await http.post<Message>(`/api/recipe/ask`, messageRequest);
  return data;
}

export async function getDetailConversation(
  conversationId: number,
  signal?: AbortSignal,
): Promise<Conversation> {
  const { data } = await http.get<Conversation>(`/api/conversation/${conversationId}`, { signal });
  return data;
}

export async function getConversations(signal?: AbortSignal): Promise<Conversation[]> {
  const { data } = await http.get<Conversation[]>(`/api/conversation`, { signal });
  return data;
}

export async function renameConversation(
  conversationId: number,
  title: string,
): Promise<Conversation> {
  const { data } = await http.patch<Conversation>(
    `/api/conversation/${conversationId}`,
    {
      title,
    },
    { headers: { 'Content-Type': 'application/json' } },
  );
  return data;
}

export async function deleteConversation(conversationId: number): Promise<void> {
  await http.delete(`/api/conversation/${conversationId}`);
}

export async function updateConversationPinned(
  conversationId: number,
  pinned: boolean,
): Promise<Conversation> {
  const { data } = await http.patch<Conversation>(
    `/api/conversation/${conversationId}/pinned`,
    {
      pinned,
    },
    { headers: { 'Content-Type': 'application/json' } },
  );
  return data;
}

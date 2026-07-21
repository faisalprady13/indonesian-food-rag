import type { Conversation, Message } from '@/types/Chat.ts';

// Mocked in-memory data standing in for a future /api/chat backend.
// Function signatures mirror src/lib/api.ts so callers can swap to real HTTP calls later.

const mockConversations: Conversation[] = [
  {
    id: 1,
    title: 'Substitutes for coconut milk in rendang',
    createdAt: '2026-07-18T09:12:00Z',
    updatedAt: '2026-07-18T09:20:00Z',
  },
  {
    id: 2,
    title: 'Another Conversation',
    createdAt: '2026-07-15T08:30:00Z',
    updatedAt: '2026-07-15T08:50:00Z',
  },
];

const mockMessagesByConversationId: Record<number, Message[]> = {
  1: [
    {
      id: 1,
      conversationId: 1,
      role: 'user',
      content: 'What can I use instead of coconut milk in rendang?',
      createdAt: '2026-07-18T09:12:00Z',
    },
    {
      id: 2,
      conversationId: 1,
      role: 'assistant',
      content:
        'You can substitute coconut milk with a mix of cashew cream and oat milk for a similar richness.',
      createdAt: '2026-07-18T09:13:00Z',
    },
  ],
  2: [
    {
      id: 3,
      conversationId: 2,
      role: 'user',
      content: 'Can you plan a week of Indonesian dinners for me?',
      createdAt: '2026-07-17T14:00:00Z',
    },
    {
      id: 4,
      conversationId: 2,
      role: 'assistant',
      content: 'Sure! Monday: Soto Ayam, Tuesday: Rendang, Wednesday: Nasi Goreng...',
      createdAt: '2026-07-17T14:02:00Z',
    },
  ],
};

export async function getConversations(signal?: AbortSignal): Promise<Conversation[]> {
  void signal;
  return mockConversations;
}

export async function getMessages(
  conversationId: number,
  signal?: AbortSignal,
): Promise<Message[]> {
  void signal;
  return mockMessagesByConversationId[conversationId] ?? [];
}

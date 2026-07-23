export type MessageRole = 'user' | 'assistant' | 'system';

export interface Conversation {
  id: number;
  title: string;
  createdAt: string;
  updatedAt: string;
}

export interface Message {
  id: number;
  conversationId: number | null;
  role: MessageRole;
  content: string;
  createdAt: string;
}

export interface MessageRequest {
  conversationId: number | null;
  content: string;
}

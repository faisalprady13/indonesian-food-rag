export type MessageRole = 'USER' | 'ASSISTANT' | 'SYSTEM';

export interface Conversation {
  id: number;
  title: string;
  createdAt: string;
  updatedAt: string;
  messages: Message[];
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

import axios from 'axios'

export interface User {
  id: number
  username: string
  fullname: string
  email: string
  imageUrl: string | null
  provider: string | null
}
export interface Recipe {
  id: number
  title: string
  steps: string
  ingredients?: string[]
  createdAt: string
  updatedAt: string
  favorited: boolean
}
export interface Page<T> {
  content: T[]
  number: number
  size: number
  totalPages: number
  totalElements: number
  first: boolean
  last: boolean
}
export interface Message {
  id: number
  conversationId: number | null
  role: 'USER' | 'ASSISTANT' | 'SYSTEM'
  content: string
  createdAt: string
}
export interface Conversation {
  id: number
  title: string
  pinned: boolean
  createdAt: string
  updatedAt: string
  messages: Message[]
}

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL || undefined,
})
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export const api = {
  async me() { return (await http.get<User>('/api/auth')).data },
  async login(username: string, password: string) {
    const token = (await http.post<string>('/api/auth/login', { username, password })).data
    if (token === 'fail') throw new Error('Invalid username or password')
    localStorage.setItem('auth_token', token)
  },
  async register(payload: Record<string, string>) {
    return (await http.post<User>('/api/auth/register', payload)).data
  },
  async recipes(page = 0, size = 10, search = '', favorites = false) {
    const path = favorites ? '/api/recipe/favorites' : '/api/recipe'
    return (await http.get<Page<Recipe>>(path, { params: { page, size, ...(search ? { search } : {}) } })).data
  },
  async recipe(id: number) { return (await http.get<Recipe>(`/api/recipe/${id}`)).data },
  async suggestions(query: string) {
    return (await http.get<{ id: number; title: string }[]>('/api/recipe/autocomplete', { params: { query, limit: 8 } })).data
  },
  async favorite(id: number, active: boolean) {
    await (active ? http.post(`/api/recipe/${id}/favorite`) : http.delete(`/api/recipe/${id}/favorite`))
  },
  async conversations() { return (await http.get<Conversation[]>('/api/conversation')).data },
  async conversation(id: number) { return (await http.get<Conversation>(`/api/conversation/${id}`)).data },
  async send(conversationId: number | null, content: string) {
    return (await http.post<Message>('/api/recipe/ask', { conversationId, content })).data
  },
  async deleteConversation(id: number) { await http.delete(`/api/conversation/${id}`) },
  async renameConversation(id: number, title: string) { return (await http.patch<Conversation>(`/api/conversation/${id}`, { title })).data },
  async settings() { return (await http.get<{ appTheme: 'light' | 'dark'; hasApiKey: boolean }>('/api/user-setting')).data },
  async keyStatus() { return (await http.get<{ isKeyAvailable: boolean }>('/api/user-setting/key-status')).data },
  async saveSettings(payload: Record<string, unknown>) { return (await http.put('/api/user-setting/', payload)).data },
  async updateUser(user: User, file?: File) {
    const data = new FormData()
    data.append('fullname', user.fullname)
    if (file) data.append('file', file)
    return (await http.put<User>(`/api/user/${user.id}`, data)).data
  },
  async deleteUser(user: User) { await http.delete(`/api/user/${user.id}/${user.username}`) },
}

import { defineStore } from 'pinia'
import { api, type Conversation, type Message, type Recipe, type User } from '../api'

export const useAppStore = defineStore('app', {
  state: () => ({
    user: null as User | null,
    loading: true,
    error: '',
    search: '',
    appliedSearch: '',
    suggestions: [] as { id: number; title: string }[],
    recipes: [] as Recipe[],
    favorites: false,
    page: 0,
    totalPages: 0,
    pageSize: 10,
    selectedRecipe: null as Recipe | null,
    conversations: [] as Conversation[],
    messages: [] as Message[],
    chatInput: '',
    sending: false,
    chatLoading: false,
    keyAvailable: true,
    profileName: '',
    profileFile: undefined as File | undefined,
    theme: 'dark' as 'light' | 'dark',
    form: { username: '', fullname: '', email: '', password: '', confirmPassword: '' },
  }),
  actions: {
    setTheme(value: 'light' | 'dark') {
      this.theme = value
      document.documentElement.classList.toggle('light', value === 'light')
      localStorage.setItem('app_theme', value)
    },
    async loadUser() {
      try {
        this.user = await api.me()
        this.profileName = this.user.fullname
        const settings = await api.settings().catch(() => null)
        if (settings?.appTheme) this.setTheme(settings.appTheme)
      } catch {
        this.user = null
      } finally {
        this.loading = false
      }
    },
    async loadRecipes() {
      const result = await api.recipes(this.page, this.pageSize, this.appliedSearch, this.favorites)
      this.recipes = result.content
      this.totalPages = result.totalPages
    },
    async loadConversations() {
      this.conversations = await api.conversations()
    },
    async loadChat(conversationId: number | null) {
      if (!conversationId) {
        this.messages = []
        return
      }
      this.chatLoading = true
      try {
        this.messages = (await api.conversation(conversationId)).messages
      } finally {
        this.chatLoading = false
      }
    },
    async initialize() {
      const savedTheme = localStorage.getItem('app_theme') as 'light' | 'dark' | null
      this.setTheme(savedTheme ?? 'dark')
      const token = new URLSearchParams(location.search).get('token')
      if (token) {
        localStorage.setItem('auth_token', token)
        history.replaceState({}, '', location.pathname)
      }
      await this.loadUser()
      if (this.user) {
        await Promise.all([
          this.loadRecipes(),
          this.loadConversations(),
          api.keyStatus().then((status) => { this.keyAvailable = status.isKeyAvailable }).catch(() => {}),
        ])
      }
    },
  },
})

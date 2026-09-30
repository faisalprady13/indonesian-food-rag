<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import { Send } from 'lucide-vue-next'
import { api } from '../api'
import { useAppStore } from '../stores/app'

const route = useRoute()
const router = useRouter()
const store = useAppStore()
const { messages, chatInput, sending, chatLoading, keyAvailable, error } = storeToRefs(store)
const conversationId = () => route.params.id ? Number(route.params.id) : null

async function sendMessage() {
  const content = chatInput.value.trim()
  if (!content || sending.value) return
  const currentId = conversationId()
  messages.value.push({ id: Date.now(), conversationId: currentId, role: 'USER', content, createdAt: new Date().toISOString() })
  chatInput.value = ''
  sending.value = true
  try {
    const reply = await api.send(currentId, content)
    messages.value.push(reply)
    await store.loadConversations()
    if (!currentId && reply.conversationId) router.replace(`/chat/${reply.conversationId}`)
  } catch {
    error.value = 'Unable to send the message.'
  } finally {
    sending.value = false
  }
}
</script>

<template>
  <section class="chat-page">
    <div class="messages">
      <p v-if="!keyAvailable" class="card notice">Add your AI provider key in your profile settings before starting a chat.</p>
      <p v-else-if="chatLoading" class="muted">Loading conversation...</p>
      <p v-else-if="!messages.length" class="muted">Start the conversation below.</p>
      <article v-for="message in messages" :key="message.id" :class="['message', message.role.toLowerCase()]"><strong>{{ message.role === 'USER' ? 'You' : 'Dish RAG' }}</strong><p>{{ message.content }}</p></article>
      <p v-if="sending" class="muted">Thinking...</p>
    </div>
    <form class="chat-input" @submit.prevent="sendMessage"><input v-model="chatInput" placeholder="Ask about Indonesian recipes..." :disabled="!keyAvailable || sending" /><button class="primary icon-button" :disabled="!keyAvailable || sending"><Send :size="18" /></button></form>
  </section>
</template>

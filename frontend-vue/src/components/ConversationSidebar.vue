<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import { Trash2 } from 'lucide-vue-next'
import { api } from '../api'
import { useAppStore } from '../stores/app'

const route = useRoute()
const router = useRouter()
const store = useAppStore()
const { conversations } = storeToRefs(store)
const conversationId = () => route.params.id ? Number(route.params.id) : null

async function remove(id: number) {
  await api.deleteConversation(id)
  conversations.value = conversations.value.filter((conversation) => conversation.id !== id)
  if (conversationId() === id) router.push('/chat')
}
</script>

<template>
  <aside class="conversation-sidebar">
    <button class="primary full" @click="router.push('/chat')">New conversation</button>
    <div v-for="conversation in conversations" :key="conversation.id" class="conversation-row" :class="{ active: conversation.id === conversationId() }">
      <a @click="router.push(`/chat/${conversation.id}`)">{{ conversation.title || 'Untitled conversation' }}</a>
      <button class="icon-button small danger" @click="remove(conversation.id)"><Trash2 :size="14" /></button>
    </div>
  </aside>
</template>

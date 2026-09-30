<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { LogOut, Menu } from 'lucide-vue-next'
import { useAppStore } from '../stores/app'
import logo from '../assets/logo.webp'

const router = useRouter()
const store = useAppStore()
const { user } = storeToRefs(store)

function logout() {
  localStorage.removeItem('auth_token')
  store.user = null
  router.push('/login')
}
</script>

<template>
  <header class="navbar">
    <button class="icon-button" title="Conversations" @click="router.push('/chat')"><Menu :size="20" /></button>
    <a class="brand" @click="router.push('/')"><img :src="logo" alt="Dish RAG" /></a>
    <div class="user-menu">
      <button class="user-button" @click="router.push('/profile')">
        <img v-if="user?.imageUrl" :src="user.imageUrl" :alt="user.username" class="avatar" />
        <span v-else class="avatar fallback">{{ user?.username?.[0]?.toUpperCase() ?? '?' }}</span>
        <span class="username">{{ user?.username }}</span>
      </button>
      <button class="icon-button danger" title="Logout" @click="logout"><LogOut :size="18" /></button>
    </div>
  </header>
</template>

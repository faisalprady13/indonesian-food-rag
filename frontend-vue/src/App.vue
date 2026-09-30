<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from './stores/app'
import AuthView from './components/AuthView.vue'
import Navbar from './components/Navbar.vue'
import ConversationSidebar from './components/ConversationSidebar.vue'
import ProfileView from './components/ProfileView.vue'
import HomeView from './components/HomeView.vue'
import ChatView from './components/ChatView.vue'
import RecipeModal from './components/RecipeModal.vue'
import { api } from './api'

const route = useRoute()
const router = useRouter()
const store = useAppStore()
const { user, loading, selectedRecipe } = storeToRefs(store)
const isAuthPage = computed(() => route.path === '/login' || route.path === '/register')
const isChat = computed(() => route.path.startsWith('/chat'))
const isProfile = computed(() => route.path === '/profile')
const conversationId = computed(() => route.params.id ? Number(route.params.id) : null)

onMounted(() => store.initialize())
watch([() => store.appliedSearch, () => store.favorites, () => store.pageSize, () => store.page], () => {
  if (user.value && !isChat.value && !isProfile.value) store.loadRecipes()
})
watch(conversationId, (id) => store.loadChat(id))
watch(() => store.search, async (value) => {
  if (value.trim().length < 3) { store.suggestions = []; return }
  store.suggestions = await api.suggestions(value.trim()).catch(() => [])
})
</script>

<template>
  <div v-if="loading" class="center"><div class="spinner" /></div>
  <AuthView v-else-if="isAuthPage" />
  <main v-else-if="!user" class="center"><button class="primary" @click="router.push('/login')">Go to login</button></main>
  <template v-else>
    <Navbar />
    <ConversationSidebar v-if="isChat" />
    <main :class="['page', { 'with-sidebar': isChat }]">
      <ProfileView v-if="isProfile" />
      <ChatView v-else-if="isChat" />
      <HomeView v-else />
    </main>
    <RecipeModal v-if="selectedRecipe" :recipe="selectedRecipe" @close="selectedRecipe = null" />
  </template>
</template>

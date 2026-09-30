<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '../stores/app'
import logo from '../assets/logo.webp'
import { api } from '../api'

const route = useRoute()
const router = useRouter()
const store = useAppStore()
const { error, form } = storeToRefs(store)

async function submit() {
  error.value = ''
  try {
    if (route.path === '/register') {
      if (form.value.password !== form.value.confirmPassword) throw new Error('Passwords do not match')
      await api.register(form.value)
    }
    await api.login(form.value.username, form.value.password)
    await store.loadUser()
    router.push('/')
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Could not complete the request.'
  }
}
</script>

<template>
  <main class="auth-shell">
    <section class="card auth-card">
      <img :src="logo" alt="Dish RAG" class="logo" />
      <h1>{{ route.path === '/login' ? 'Login to your account' : 'Create an account' }}</h1>
      <p class="muted">{{ route.path === '/login' ? 'Enter your username below to login to your account' : 'Enter your information below to create your account' }}</p>
      <form @submit.prevent="submit">
        <label>Username<input v-model="form.username" placeholder="johndoe" required /></label>
        <template v-if="route.path === '/register'">
          <label>Full Name<input v-model="form.fullname" placeholder="John Doe" required /></label>
          <label>Email<input v-model="form.email" type="email" placeholder="m@example.com" required /></label>
        </template>
        <label>Password<input v-model="form.password" type="password" :minlength="route.path === '/register' ? 8 : undefined" required /></label>
        <label v-if="route.path === '/register'">Confirm Password<input v-model="form.confirmPassword" type="password" minlength="8" required /></label>
        <p v-if="error" class="error">{{ error }}</p>
        <button class="primary" type="submit">{{ route.path === '/login' ? 'Login' : 'Create Account' }}</button>
      </form>
      <p class="muted">{{ route.path === '/login' ? "Don't have an account?" : 'Already have an account?' }}
        <a @click="router.push(route.path === '/login' ? '/register' : '/login')">{{ route.path === '/login' ? 'Sign up' : 'Sign in' }}</a>
      </p>
    </section>
  </main>
</template>

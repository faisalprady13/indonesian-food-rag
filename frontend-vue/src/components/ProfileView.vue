<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { Sun, Moon } from 'lucide-vue-next'
import { api } from '../api'
import { useAppStore } from '../stores/app'

const store = useAppStore()
const { user, profileName, profileFile, theme } = storeToRefs(store)

async function saveProfile() {
  if (!user.value) return
  user.value = await api.updateUser({ ...user.value, fullname: profileName.value }, profileFile.value)
}

async function saveTheme() {
  await api.saveSettings({ appTheme: theme.value })
  store.setTheme(theme.value)
}
</script>

<template>
  <section class="profile card">
    <h1>My profile</h1>
    <label>Username<input :value="user?.username" disabled /></label>
    <label>Full name<input v-model="profileName" /></label>
    <label>Profile image<input type="file" accept="image/*" @change="profileFile = ($event.target as HTMLInputElement).files?.[0]" /></label>
    <button class="primary" @click="saveProfile">Save profile</button>
    <hr />
    <h2>Appearance</h2>
    <div class="theme-row"><button :class="{ selected: theme === 'light' }" @click="theme = 'light'"><Sun :size="16" /> Light</button><button :class="{ selected: theme === 'dark' }" @click="theme = 'dark'"><Moon :size="16" /> Dark</button></div>
    <button class="secondary" @click="saveTheme">Save appearance</button>
  </section>
</template>

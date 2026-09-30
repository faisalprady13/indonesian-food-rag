<script setup lang="ts">
import { X } from 'lucide-vue-next'
import type { Recipe } from '../api'

defineProps<{ recipe: Recipe }>()
const emit = defineEmits<{ close: [] }>()
</script>

<template>
  <div class="modal-backdrop" @click.self="emit('close')">
    <article class="modal card">
      <button class="close icon-button" @click="emit('close')"><X :size="18" /></button>
      <h1>{{ recipe.title }}</h1>
      <h3>Ingredients</h3>
      <ul><li v-for="ingredient in recipe.ingredients" :key="ingredient">{{ ingredient }}</li></ul>
      <h3>Steps</h3>
      <p class="recipe-steps">{{ recipe.steps }}</p>
    </article>
  </div>
</template>

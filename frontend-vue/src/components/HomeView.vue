<script setup lang="ts">
import { storeToRefs } from 'pinia';
import { Search, X, Heart, ChevronLeft, ChevronRight } from 'lucide-vue-next';
import { api, type Recipe } from '../api';
import { useAppStore } from '../stores/app';

const store = useAppStore();
const {
  search,
  suggestions,
  recipes,
  favorites,
  page,
  totalPages,
  pageSize,
  selectedRecipe,
} = storeToRefs(store);

function applySearch(value = search.value) {
  store.appliedSearch = value.trim();
  search.value = store.appliedSearch;
  page.value = 0;
  suggestions.value = [];
}

async function toggleFavorite(recipe: Recipe) {
  await api.favorite(recipe.id, !recipe.favorited);
  recipe.favorited = !recipe.favorited;
}

async function openRecipe(recipe: Recipe) {
  selectedRecipe.value = await api.recipe(recipe.id);
}

function formatDate(value: string) {
  return new Date(value).toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
}
</script>

<template>
  <section class="home">
    <div class="search-box">
      <Search :size="17" class="search-icon" /><input
        v-model="search"
        placeholder="Search recipes by title..."
        @keyup.enter="applySearch()"
      /><button
        v-if="search"
        class="clear"
        @click="
          search = '';
          applySearch('');
        "
      >
        <X :size="16" />
      </button>
      <div v-if="suggestions.length" class="suggestions">
        <button
          v-for="item in suggestions"
          :key="item.id"
          @click="applySearch(item.title)"
        >
          {{ item.title }}
        </button>
      </div>
    </div>
    <div class="tabs">
      <button :class="{ selected: !favorites }" @click="favorites = false">
        All recipes</button
      ><button :class="{ selected: favorites }" @click="favorites = true">
        Favorites
      </button>
    </div>
    <div class="recipe-grid">
      <article
        v-for="recipe in recipes"
        :key="recipe.id"
        class="card recipe-card"
        @click="openRecipe(recipe)"
      >
        <div class="recipe-title">
          <h2>{{ recipe.title }}</h2>
          <button class="icon-button" @click.stop="toggleFavorite(recipe)">
            <Heart
              :size="18"
              :fill="recipe.favorited ? 'currentColor' : 'none'"
            />
          </button>
        </div>
        <p class="muted">
          {{
            recipe.ingredients?.slice(0, 3).join(', ') || 'Indonesian cuisine'
          }}
        </p>
        <small class="muted">{{ formatDate(recipe.updatedAt) }}</small>
      </article>
    </div>
    <p v-if="!recipes.length" class="muted empty">No recipes found.</p>
    <div class="pagination">
      <button class="icon-button" :disabled="page === 0" @click="page--">
        <ChevronLeft :size="18" /></button
      ><span>Page {{ page + 1 }} of {{ Math.max(totalPages, 1) }}</span
      ><button
        class="icon-button"
        :disabled="page + 1 >= totalPages"
        @click="page++"
      >
        <ChevronRight :size="18" /></button
      ><select v-model.number="pageSize">
        <option :value="10">10</option>
        <option :value="20">20</option>
        <option :value="50">50</option>
      </select>
    </div>
  </section>
</template>

import { http } from '@/queries/http.ts';
import type {
  Recipe,
  Page,
  RecipeSuggestion,
  GetRecipesParams,
  GetSelectedRecipeParams,
  GetFavoriteRecipeParams,
} from '@/types/Recipe.ts';

export async function getRecipes(
  params: GetRecipesParams = {},
  signal?: AbortSignal,
): Promise<Page<Recipe>> {
  const { data } = await http.get<Page<Recipe>>('/api/recipe', {
    params,
    signal,
  });

  return data;
}

export async function getSelectedRecipe(
  { id }: GetSelectedRecipeParams,
  signal?: AbortSignal,
): Promise<Recipe> {
  const { data } = await http.get<Recipe>(`/api/recipe/${id}`, {
    signal,
  });

  return data;
}

export async function autocompleteRecipes(
  query: string,
  limit = 8,
  signal?: AbortSignal,
): Promise<RecipeSuggestion[]> {
  if (!query.trim()) {
    return [];
  }

  const { data } = await http.get<RecipeSuggestion[]>('/api/recipe/autocomplete', {
    params: { query, limit },
    signal,
  });

  return data;
}

export async function getFavoriteRecipesByUserId(
  params: GetFavoriteRecipeParams = {},
  signal?: AbortSignal,
): Promise<Page<Recipe>> {
  const { data } = await http.get<Page<Recipe>>('/api/recipe/favorites', {
    params,
    signal,
  });

  return data;
}

export async function addFavoriteRecipe(id: number): Promise<void> {
  await http.post(`/api/recipe/${id}/favorite`);
}

export async function removeFavoriteRecipe(id: number): Promise<void> {
  await http.delete(`/api/recipe/${id}/favorite`);
}

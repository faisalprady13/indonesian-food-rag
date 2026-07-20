import axios from 'axios';
import type {
  Recipe,
  Page,
  RecipeSuggestion,
  GetRecipesParams,
  GetSelectedRecipeParams,
  GetFavoriteRecipeParams,
} from '../types/Recipe.ts';

export interface CurrentUser {
  id: number;
  username: string;
  fullname: string;
  email: string;
  imageUrl: string | null;
  provider: string | null;
}

const TOKEN_KEY = 'auth_token';

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}

const http = axios.create();

http.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export async function login(username: string, password: string): Promise<void> {
  const { data: token } = await http.post<string>('/api/auth/login', { username, password });
  if (token === 'fail') {
    throw new Error('Invalid username or password');
  }
  setToken(token);
}

export interface RegisterPayload {
  username: string;
  email: string;
  password: string;
  fullname: string;
}

export async function register(payload: RegisterPayload): Promise<CurrentUser> {
  const { data } = await http.post<CurrentUser>('/api/auth/register', payload);
  return data;
}

export async function getMe(): Promise<CurrentUser> {
  const { data } = await http.get<CurrentUser>('/api/auth');
  return data;
}

export async function updateUserApi(user: CurrentUser, file?: File): Promise<CurrentUser> {
  const formData = new FormData();
  if (file) {
    formData.append('file', file);
  }
  formData.append('fullname', user.fullname);

  const { data } = await http.put<CurrentUser>(`/api/user/${user.id}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  return data;
}

export async function deleteUserApi(id: number, username: string): Promise<void> {
  await http.delete(`/api/user/${id}/${username}`);
}

export function logout(): void {
  clearToken();
}

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

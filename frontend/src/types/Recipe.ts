export interface Recipe {
  id: number;
  title: string;
  steps: string;
  ingredients?: string[];
  createdAt: string;
  updatedAt: string;
  favorited: boolean;
}

export interface RecipeSuggestion {
  id: number;
  title: string;
}

export interface Page<T> {
  content: T[];
  number: number;
  size: number;
  totalPages: number;
  totalElements: number;
  first: boolean;
  last: boolean;
}

export function emptyPage<T>(size: number): Page<T> {
  return {
    content: [],
    number: 0,
    size,
    totalPages: 0,
    totalElements: 0,
    first: true,
    last: true,
  };
}

export interface GetRecipesParams {
  page?: number;
  size?: number;
  sortBy?: string;
  direction?: 'asc' | 'desc';
  search?: string;
}

export interface GetSelectedRecipeParams {
  id: number;
}

export interface GetFavoriteRecipeParams {
  page?: number;
  size?: number;
  search?: string;
}

export type RecipeTab = 'all' | 'favorites';

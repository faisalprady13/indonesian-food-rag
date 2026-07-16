export interface Recipe {
  id: number;
  title: string;
  steps: string;
  ingredients?: string[];
  createdAt: string;
  updatedAt: string;
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

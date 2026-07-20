import { useEffect, useState } from 'react';
import { Star } from 'lucide-react';
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs.tsx';
import RecipeDetailDialog from '@/components/recipeDetailDialog/RecipeDetailDialog.tsx';
import RecipeList from '@/components/recipeList/RecipeList.tsx';
import {
  addFavoriteRecipe,
  getFavoriteRecipesByUserId,
  getRecipes,
  getSelectedRecipe,
  removeFavoriteRecipe,
} from '@/lib/api.ts';
import { useAsyncData } from '@/hooks/useAsyncData.ts';
import { PAGE_SIZE } from '@/constants/page.ts';

import type { Page, Recipe, RecipeTab } from '@/types/Recipe.ts';

interface RecipeTabsProps {
  appliedSearch: string;
}

export default function RecipeTabs({ appliedSearch }: RecipeTabsProps) {
  const [activeTab, setActiveTab] = useState<RecipeTab>('all');

  const [page, setPage] = useState(0);
  const [favoritesPageNum, setFavoritesPageNum] = useState(0);
  const [pageSize, setPageSize] = useState(PAGE_SIZE);
  const [selectedRecipeId, setSelectedRecipeId] = useState<number | null>(null);

  useEffect(() => {
    setPage(0);
    setFavoritesPageNum(0);
  }, [appliedSearch, pageSize]);

  const [recipesPage, loading, setRecipesPage] = useAsyncData<Page<Recipe>>(
    (signal) => getRecipes({ page, size: pageSize, search: appliedSearch }, signal),
    [page, pageSize, appliedSearch],
  );

  const [favoritesPage, loadingFavorites, setFavoritesPage] = useAsyncData<Page<Recipe>>(
    (signal) =>
      getFavoriteRecipesByUserId(
        { page: favoritesPageNum, size: pageSize, search: appliedSearch },
        signal,
      ),
    [favoritesPageNum, pageSize, appliedSearch],
    activeTab === 'favorites',
  );

  const [selectedRecipe, loadingSelectedRecipe, setSelectedRecipe] = useAsyncData<Recipe>(
    (signal) => getSelectedRecipe({ id: selectedRecipeId as number }, signal),
    [selectedRecipeId],
    selectedRecipeId !== null,
  );

  function updateFavorited(recipeId: number, favorited: boolean) {
    const applyToPage = (p: Page<Recipe> | null) =>
      p
        ? {
            ...p,
            content: p.content.map((r) => (r.id === recipeId ? { ...r, favorited } : r)),
          }
        : p;
    setRecipesPage(applyToPage);
    setFavoritesPage(applyToPage);
    setSelectedRecipe((r) => (r && r.id === recipeId ? { ...r, favorited } : r));
  }

  async function handleToggleFavorite(recipe: Recipe) {
    const nextFavorited = !recipe.favorited;
    updateFavorited(recipe.id, nextFavorited);
    try {
      if (nextFavorited) {
        await addFavoriteRecipe(recipe.id);
      } else {
        await removeFavoriteRecipe(recipe.id);
      }
    } catch {
      updateFavorited(recipe.id, recipe.favorited);
    }
  }

  return (
    <>
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList>
          <TabsTrigger value="all">All</TabsTrigger>
          <TabsTrigger value="favorites">
            <Star />
            Your Favorites
          </TabsTrigger>
        </TabsList>
        <TabsContent value="all">
          <RecipeList
            recipesPage={recipesPage}
            loading={loading}
            appliedSearch={appliedSearch}
            onSelectRecipe={setSelectedRecipeId}
            onPageChange={setPage}
            pageSize={pageSize}
            onPageSizeChange={setPageSize}
            onToggleFavorite={handleToggleFavorite}
            activeTab={activeTab}
          />
        </TabsContent>
        <TabsContent value="favorites">
          <RecipeList
            recipesPage={favoritesPage}
            loading={loadingFavorites}
            appliedSearch={appliedSearch}
            onSelectRecipe={setSelectedRecipeId}
            onPageChange={setFavoritesPageNum}
            pageSize={pageSize}
            onPageSizeChange={setPageSize}
            onToggleFavorite={handleToggleFavorite}
            activeTab={activeTab}
          />
        </TabsContent>
      </Tabs>

      <RecipeDetailDialog
        open={selectedRecipeId !== null}
        onOpenChange={(open) => {
          if (!open) {
            setSelectedRecipeId(null);
          }
        }}
        loading={loadingSelectedRecipe}
        recipe={selectedRecipe}
        onToggleFavorite={handleToggleFavorite}
      />
    </>
  );
}

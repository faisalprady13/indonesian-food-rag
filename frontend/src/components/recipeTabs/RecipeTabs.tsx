import { useEffect, useState } from 'react';
import { Star } from 'lucide-react';
import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
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
import { PAGE_SIZE } from '@/constants/page.ts';

import { emptyPage, type Page, type Recipe, type RecipeTab } from '@/types/Recipe.ts';

function applyFavorite(recipeId: number, favorited: boolean) {
  return (old: Page<Recipe> | Recipe | undefined) => {
    if (!old) return old;
    if ('content' in old) {
      return {
        ...old,
        content: old.content.map((r) => (r.id === recipeId ? { ...r, favorited } : r)),
      };
    }
    return old.id === recipeId ? { ...old, favorited } : old;
  };
}

interface RecipeTabsProps {
  appliedSearch: string;
}

export default function RecipeTabs({ appliedSearch }: RecipeTabsProps) {
  const [activeTab, setActiveTab] = useState<RecipeTab>('all');

  const [page, setPage] = useState(0);
  const [favoritesPageNum, setFavoritesPageNum] = useState(0);
  const [pageSize, setPageSize] = useState(PAGE_SIZE);
  const [selectedRecipeId, setSelectedRecipeId] = useState<number | null>(null);

  const queryClient = useQueryClient();

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setPage(0);
    setFavoritesPageNum(0);
  }, [appliedSearch]);

  const handlePageSizeChange = (value: number) => {
    setPageSize(value);
    setActiveTab(activeTab);
    setPage(0);
    setFavoritesPageNum(0);
  };

  const { data: recipesPage, isLoading: loading } = useQuery({
    queryKey: ['recipes', 'list', { page, pageSize, appliedSearch }],
    queryFn: ({ signal }) => getRecipes({ page, size: pageSize, search: appliedSearch }, signal),
    placeholderData: keepPreviousData,
  });

  const { data: favoritesPage, isLoading: loadingFavorites } = useQuery({
    queryKey: ['recipes', 'favorites', { page: favoritesPageNum, pageSize, appliedSearch }],
    queryFn: ({ signal }) =>
      getFavoriteRecipesByUserId(
        { page: favoritesPageNum, size: pageSize, search: appliedSearch },
        signal,
      ),
    enabled: activeTab === 'favorites',
    placeholderData: keepPreviousData,
  });

  const { data: selectedRecipe, isLoading: loadingSelectedRecipe } = useQuery({
    queryKey: ['recipes', 'detail', selectedRecipeId],
    queryFn: ({ signal }) => getSelectedRecipe({ id: selectedRecipeId as number }, signal),
    enabled: selectedRecipeId !== null,
  });

  const favoriteMutation = useMutation({
    mutationFn: (recipe: Recipe) =>
      recipe.favorited ? removeFavoriteRecipe(recipe.id) : addFavoriteRecipe(recipe.id),
    onMutate: async (recipe: Recipe) => {
      await queryClient.cancelQueries({ queryKey: ['recipes'] });
      const previous = queryClient.getQueriesData({ queryKey: ['recipes'] });
      queryClient.setQueriesData(
        { queryKey: ['recipes'] },
        applyFavorite(recipe.id, !recipe.favorited),
      );
      return { previous };
    },
    onError: (_err, _recipe, context) => {
      context?.previous.forEach(([key, data]) => queryClient.setQueryData(key, data));
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['recipes'] });
    },
  });

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
            recipesPage={recipesPage ?? emptyPage(pageSize)}
            loading={loading}
            appliedSearch={appliedSearch}
            onSelectRecipe={setSelectedRecipeId}
            onPageChange={setPage}
            pageSize={pageSize}
            onPageSizeChange={handlePageSizeChange}
            onToggleFavorite={favoriteMutation.mutate}
            activeTab={activeTab}
          />
        </TabsContent>
        <TabsContent value="favorites">
          <RecipeList
            recipesPage={favoritesPage ?? emptyPage(pageSize)}
            loading={loadingFavorites}
            appliedSearch={appliedSearch}
            onSelectRecipe={setSelectedRecipeId}
            onPageChange={setFavoritesPageNum}
            pageSize={pageSize}
            onPageSizeChange={handlePageSizeChange}
            onToggleFavorite={favoriteMutation.mutate}
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
        recipe={selectedRecipe ?? null}
        onToggleFavorite={favoriteMutation.mutate}
      />
    </>
  );
}

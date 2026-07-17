import { HugeiconsIcon } from '@hugeicons/react';
import { ArrowLeft01Icon, ArrowRight01Icon } from '@hugeicons/core-free-icons';
import { Star } from 'lucide-react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card.tsx';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table.tsx';
import { Button } from '@/components/ui/button.tsx';
import RecipeTableSkeleton from '@/components/skeletons/RecipeTableSkeleton.tsx';

import type { Page, Recipe, RecipeTab } from '@/types/Recipe.ts';

interface RecipeListProps {
  recipesPage: Page<Recipe> | null;
  loading: boolean;
  appliedSearch: string;
  onSelectRecipe: (id: number) => void;
  onPageChange: (updater: (page: number) => number) => void;
  onToggleFavorite: (recipe: Recipe) => void;
  activeTab: RecipeTab;
}

export default function RecipeList({
  recipesPage,
  loading,
  appliedSearch,
  onSelectRecipe,
  onPageChange,
  onToggleFavorite,
  activeTab,
}: RecipeListProps) {
  return (
    <Card className="w-full">
      <CardHeader>
        <CardTitle>Recipes</CardTitle>
        <CardDescription>
          {appliedSearch ? `Showing results for "${appliedSearch}"` : ''}
        </CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-4">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-8" />
              <TableHead>Title</TableHead>
              <TableHead>Created</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <RecipeTableSkeleton rows={recipesPage?.size ?? 10} />
            ) : recipesPage && recipesPage.content.length > 0 ? (
              recipesPage.content.map((recipe) => (
                <TableRow
                  className="cursor-pointer text-left"
                  key={recipe.id}
                  role="button"
                  tabIndex={0}
                  onClick={() => onSelectRecipe(recipe.id)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' || e.key === ' ') {
                      e.preventDefault();
                      onSelectRecipe(recipe.id);
                    }
                  }}
                >
                  <TableCell>
                    <button
                      type="button"
                      onClick={(e) => {
                        e.stopPropagation();
                        onToggleFavorite(recipe);
                      }}
                      aria-label={recipe.favorited ? 'Remove from favorites' : 'Add to favorites'}
                      aria-pressed={recipe.favorited}
                      className="text-muted-foreground hover:text-foreground"
                    >
                      <Star
                        size={16}
                        className={recipe.favorited ? 'fill-current text-yellow-500' : ''}
                      />
                    </button>
                  </TableCell>
                  <TableCell className="font-medium">{recipe.title}</TableCell>
                  <TableCell className="whitespace-nowrap text-muted-foreground">
                    {new Date(recipe.createdAt).toLocaleDateString()}
                  </TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={3} className="text-center text-muted-foreground">
                  {activeTab === 'all'
                    ? 'No recipes found.'
                    : "The recipe you're looking for is not in your favorites."}
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>

        {recipesPage && recipesPage.totalElements > 0 && (
          <div className="flex items-center justify-between">
            <span className="text-sm text-muted-foreground">
              Page {recipesPage.number + 1} of {recipesPage.totalPages} ·{' '}
              {recipesPage.totalElements} recipes
            </span>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="icon-sm"
                disabled={recipesPage.first || loading}
                onClick={() => onPageChange((p) => Math.max(0, p - 1))}
                aria-label="Previous page"
              >
                <HugeiconsIcon icon={ArrowLeft01Icon} size={16} />
              </Button>
              <Button
                variant="outline"
                size="icon-sm"
                disabled={recipesPage.last || loading}
                onClick={() => onPageChange((p) => p + 1)}
                aria-label="Next page"
              >
                <HugeiconsIcon icon={ArrowRight01Icon} size={16} />
              </Button>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

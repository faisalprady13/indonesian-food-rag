import { useEffect, useState } from 'react';
import { Settings, Star } from 'lucide-react';
import {
  Card,
  CardAction,
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
import { Label } from '@/components/ui/label.tsx';
import { NumberField, NumberFieldGroup, NumberFieldInput } from '@/components/ui/number-field.tsx';
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination.tsx';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover.tsx';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select.tsx';
import RecipeTableSkeleton from '@/components/skeletons/RecipeTableSkeleton.tsx';
import { PAGE_SIZE_OPTIONS } from '@/constants/page.ts';

import type { Page, Recipe, RecipeTab } from '@/types/Recipe.ts';

function getPageNumbers(current: number, total: number): (number | 'ellipsis')[] {
  const delta = 1;
  const pages: number[] = [];

  for (let i = 0; i < total; i++) {
    if (i === 0 || i === total - 1 || (i >= current - delta && i <= current + delta)) {
      pages.push(i);
    }
  }

  const result: (number | 'ellipsis')[] = [];
  let previous: number | null = null;
  for (const page of pages) {
    if (previous !== null) {
      if (page - previous === 2) {
        result.push(previous + 1);
      } else if (page - previous > 2) {
        result.push('ellipsis');
      }
    }
    result.push(page);
    previous = page;
  }
  return result;
}

interface RecipeListProps {
  recipesPage: Page<Recipe> | null;
  loading: boolean;
  appliedSearch: string;
  onSelectRecipe: (id: number) => void;
  onPageChange: (updater: (page: number) => number) => void;
  pageSize: number;
  onPageSizeChange: (updater: (size: number) => number) => void;
  onToggleFavorite: (recipe: Recipe) => void;
  activeTab: RecipeTab;
}

export default function RecipeList({
  recipesPage,
  loading,
  appliedSearch,
  onSelectRecipe,
  onPageChange,
  pageSize,
  onPageSizeChange,
  onToggleFavorite,
  activeTab,
}: RecipeListProps) {
  const [pageValue, setPageValue] = useState<number | null>(null);

  const totalPages = recipesPage?.totalPages ?? 0;

  useEffect(() => {
    if (recipesPage) {
      setPageValue(recipesPage.number + 1);
    }
  }, [recipesPage?.number]);

  const handlePageCommit = (value: number | null) => {
    if (value === null) {
      return;
    }
    onPageChange(() => value - 1);
  };

  return (
    <Card className="w-full">
      <CardHeader>
        <CardTitle>Recipes</CardTitle>
        <CardDescription>
          {appliedSearch ? `Showing results for "${appliedSearch}"` : ''}
        </CardDescription>
        <CardAction>
          <Popover>
            <PopoverTrigger
              aria-label="Table settings"
              className="inline-flex h-8 w-8 items-center justify-center rounded-md border border-input text-muted-foreground transition-colors outline-none hover:bg-muted hover:text-foreground focus-visible:ring-3 focus-visible:ring-ring/50 aria-expanded:bg-muted aria-expanded:text-foreground"
            >
              <Settings size={16} />
            </PopoverTrigger>
            <PopoverContent className="flex w-56 flex-col gap-3">
              <div className="flex flex-col gap-1">
                <Label htmlFor="page-size-trigger">Rows per page</Label>
                <Select
                  value={pageSize}
                  disabled={loading}
                  onValueChange={(size) => {
                    if (!!size) {
                      onPageSizeChange(() => size);
                    }
                  }}
                >
                  <SelectTrigger id="page-size-trigger">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {PAGE_SIZE_OPTIONS.map((option) => (
                      <SelectItem key={option} value={option}>
                        {option}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="flex flex-col gap-1">
                <Label htmlFor="page-number-input">Page number</Label>
                <NumberField
                  value={pageValue}
                  onValueChange={setPageValue}
                  onValueCommitted={handlePageCommit}
                  min={1}
                  max={totalPages || 1}
                  disabled={loading}
                >
                  <NumberFieldGroup>
                    <NumberFieldInput
                      id="page-number-input"
                      onKeyDown={(e) => {
                        if (e.key === 'Enter') {
                          e.preventDefault();
                          e.currentTarget.blur();
                        }
                      }}
                    />
                  </NumberFieldGroup>
                </NumberField>
              </div>
            </PopoverContent>
          </Popover>
        </CardAction>
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
          <div className="flex flex-col items-center gap-2 sm:flex-row sm:justify-between">
            <span className="text-sm text-muted-foreground">
              Page {recipesPage.number + 1} of {recipesPage.totalPages} ·{' '}
              {recipesPage.totalElements} recipes
            </span>
            <Pagination className="mx-0 w-auto">
              <PaginationContent>
                <PaginationItem>
                  <PaginationPrevious
                    disabled={recipesPage.first || loading}
                    onClick={() => onPageChange((p) => Math.max(0, p - 1))}
                  />
                </PaginationItem>
                {getPageNumbers(recipesPage.number, recipesPage.totalPages).map((page, index) =>
                  page === 'ellipsis' ? (
                    <PaginationItem key={`ellipsis-${index}`}>
                      <PaginationEllipsis />
                    </PaginationItem>
                  ) : (
                    <PaginationItem key={page}>
                      <PaginationLink
                        isActive={page === recipesPage.number}
                        disabled={loading}
                        onClick={() => onPageChange(() => page)}
                      >
                        {page + 1}
                      </PaginationLink>
                    </PaginationItem>
                  ),
                )}
                <PaginationItem>
                  <PaginationNext
                    disabled={recipesPage.last || loading}
                    onClick={() => onPageChange((p) => p + 1)}
                  />
                </PaginationItem>
              </PaginationContent>
            </Pagination>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

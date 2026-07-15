import { useEffect, useRef, useState } from 'react';
import { HugeiconsIcon } from '@hugeicons/react';
import {
  ArrowLeft01Icon,
  ArrowRight01Icon,
  Cancel01Icon,
  Search01Icon,
} from '@hugeicons/core-free-icons';
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
import { Input } from '@/components/ui/input.tsx';
import { Button } from '@/components/ui/button.tsx';
import { Skeleton } from '@/components/ui/skeleton.tsx';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog.tsx';
import { autocompleteRecipes, getRecipes, type CurrentUser } from '@/lib/api.ts';
import type { Page, Recipe, RecipeSuggestion } from '@/types/Recipe.ts';

import { PAGE_SIZE } from '../constants/page.ts';
import { DEBOUNCE_300_MS } from '@/constants/debounce.ts';

type HomeProps = {
  user: CurrentUser;
};

export default function Home({ user }: Readonly<HomeProps>) {
  const [searchInput, setSearchInput] = useState('');
  const [appliedSearch, setAppliedSearch] = useState('');
  const [suggestions, setSuggestions] = useState<RecipeSuggestion[]>([]);
  const [showSuggestions, setShowSuggestions] = useState(false);

  const [page, setPage] = useState(0);
  const [recipesPage, setRecipesPage] = useState<Page<Recipe> | null>(null);
  const [loading, setLoading] = useState(true);
  const [selectedRecipe, setSelectedRecipe] = useState<Recipe | null>(null);

  const searchBoxRef = useRef<HTMLDivElement>(null);
  const suggestionRequestId = useRef(0);
  const recipesRequestId = useRef(0);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (searchBoxRef.current && !searchBoxRef.current.contains(event.target as Node)) {
        setShowSuggestions(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    const query = searchInput.trim();
    if (query.length < 3) {
      setSuggestions([]);
      return;
    }
    const requestId = ++suggestionRequestId.current;
    const timeout = setTimeout(() => {
      autocompleteRecipes(query).then((results) => {
        if (requestId === suggestionRequestId.current) {
          setSuggestions(results);
        }
      });
    }, DEBOUNCE_300_MS);
    return () => clearTimeout(timeout);
  }, [searchInput]);

  useEffect(() => {
    const requestId = ++recipesRequestId.current;
    setLoading(true);
    getRecipes({ page, size: PAGE_SIZE, search: appliedSearch }).then((data) => {
      if (requestId === recipesRequestId.current) {
        setRecipesPage(data);
        setLoading(false);
      }
    });
  }, [page, appliedSearch]);

  function applySearch(value: string) {
    setAppliedSearch(value.trim());
    setPage(0);
    setShowSuggestions(false);
  }

  function handleSelectSuggestion(suggestion: RecipeSuggestion) {
    setSearchInput(suggestion.title);
    applySearch(suggestion.title);
  }

  function handleClearSearch() {
    setSearchInput('');
    applySearch('');
  }

  return (
    <div className="mx-auto flex w-full max-w-7xl flex-1 flex-col gap-6">
      <div ref={searchBoxRef} className="relative mx-auto w-full max-w-md">
        <div className="relative">
          <HugeiconsIcon
            icon={Search01Icon}
            size={16}
            className="pointer-events-none absolute top-1/2 left-2.5 -translate-y-1/2 text-muted-foreground"
          />
          <Input
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            onFocus={() => setShowSuggestions(true)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                applySearch(searchInput);
              }
            }}
            placeholder="Search recipes by title..."
            className="pl-8 pr-8"
          />
          {searchInput && (
            <button
              type="button"
              onClick={handleClearSearch}
              aria-label="Clear search"
              className="absolute top-1/2 right-2.5 -translate-y-1/2 text-muted-foreground hover:text-foreground"
            >
              <HugeiconsIcon icon={Cancel01Icon} size={14} />
            </button>
          )}
        </div>

        {showSuggestions && suggestions.length > 0 && (
          <div className="absolute z-10 mt-1 w-full overflow-hidden rounded-md border border-border bg-popover text-popover-foreground shadow-md">
            {suggestions.map((suggestion) => (
              <button
                key={suggestion.id}
                type="button"
                onClick={() => handleSelectSuggestion(suggestion)}
                className="block w-full px-3 py-2 text-left text-sm hover:bg-muted"
              >
                {suggestion.title}
              </button>
            ))}
          </div>
        )}
      </div>

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
                <TableHead>Title</TableHead>
                <TableHead>Ingredients</TableHead>
                <TableHead>Created</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {loading ? (
                Array.from({ length: PAGE_SIZE }, (_, index) => (
                  <TableRow key={index}>
                    <TableCell>
                      <Skeleton className="h-4 w-32" />
                    </TableCell>
                    <TableCell>
                      <Skeleton className="h-4 w-48" />
                    </TableCell>
                    <TableCell>
                      <Skeleton className="h-4 w-20" />
                    </TableCell>
                  </TableRow>
                ))
              ) : recipesPage && recipesPage.content.length > 0 ? (
                recipesPage.content.map((recipe) => (
                  <TableRow
                    className="cursor-pointer text-left"
                    key={recipe.id}
                    role="button"
                    tabIndex={0}
                    onClick={() => setSelectedRecipe(recipe)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter' || e.key === ' ') {
                        e.preventDefault();
                        setSelectedRecipe(recipe);
                      }
                    }}
                  >
                    <TableCell className="max-w-xs truncate font-medium">{recipe.title}</TableCell>
                    <TableCell className="max-w-xs truncate text-muted-foreground">
                      {recipe.ingredients.join(', ') || '—'}
                    </TableCell>
                    <TableCell className="whitespace-nowrap text-muted-foreground">
                      {new Date(recipe.createdAt).toLocaleDateString()}
                    </TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={3} className="text-center text-muted-foreground">
                    No recipes found.
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
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  aria-label="Previous page"
                >
                  <HugeiconsIcon icon={ArrowLeft01Icon} size={16} />
                </Button>
                <Button
                  variant="outline"
                  size="icon-sm"
                  disabled={recipesPage.last || loading}
                  onClick={() => setPage((p) => p + 1)}
                  aria-label="Next page"
                >
                  <HugeiconsIcon icon={ArrowRight01Icon} size={16} />
                </Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      <Dialog
        open={selectedRecipe !== null}
        onOpenChange={(open) => {
          if (!open) {
            setSelectedRecipe(null);
          }
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{selectedRecipe?.title}</DialogTitle>
            <DialogDescription className="text-xs">
              {selectedRecipe &&
                `Created ${new Date(selectedRecipe.createdAt).toLocaleDateString()}`}
            </DialogDescription>
          </DialogHeader>
          <div className="flex flex-col gap-4">
            <div>
              <h3 className="mb-1.5 text-sm font-medium">Ingredients</h3>
              {selectedRecipe?.ingredients.length ? (
                <ul className="list-inside list-disc text-sm text-muted-foreground">
                  {selectedRecipe.ingredients.map((ingredient, index) => (
                    <li key={index}>{ingredient}</li>
                  ))}
                </ul>
              ) : (
                <p className="text-sm text-muted-foreground">No ingredients listed.</p>
              )}
            </div>
            <div>
              <h3 className="mb-1.5 text-sm font-medium">Steps</h3>
              <p className="whitespace-pre-line text-sm text-muted-foreground">
                {selectedRecipe?.steps || 'No steps listed.'}
              </p>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}

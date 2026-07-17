import { useEffect, useRef, useState } from 'react';
import { HugeiconsIcon } from '@hugeicons/react';
import { Cancel01Icon, Search01Icon } from '@hugeicons/core-free-icons';
import { Input } from '@/components/ui/input.tsx';
import RecipeTabs from '@/components/recipeTabs/RecipeTabs.tsx';
import { autocompleteRecipes } from '@/lib/api.ts';

import type { RecipeSuggestion } from '@/types/Recipe.ts';

import { DEBOUNCE_300_MS } from '@/constants/debounce.ts';

export default function Home() {
  const [searchInput, setSearchInput] = useState('');
  const [appliedSearch, setAppliedSearch] = useState('');
  const [suggestions, setSuggestions] = useState<RecipeSuggestion[]>([]);
  const [showSuggestions, setShowSuggestions] = useState(false);

  const searchBoxRef = useRef<HTMLDivElement>(null);

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

    const controller = new AbortController();

    const timeout = setTimeout(() => {
      autocompleteRecipes(query, 8, controller.signal)
        .then((results) => {
          setSuggestions(results);
        })
        .catch((error) => {
          if (error.name !== 'CanceledError') {
            console.error(error);
          }
        });
    }, DEBOUNCE_300_MS);

    return () => {
      clearTimeout(timeout);
      controller.abort();
    };
  }, [searchInput]);

  function applySearch(value: string) {
    setAppliedSearch(value.trim());
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
    <div className="mx-auto flex w-full max-w-4xl flex-1 flex-col gap-6">
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
      <RecipeTabs appliedSearch={appliedSearch} />
    </div>
  );
}

import { Star } from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog.tsx';
import RecipeDetailDialogSkeleton from '@/components/skeletons/RecipeDetailDialogSkeleton.tsx';
import type { Recipe } from '@/types/Recipe.ts';

interface RecipeDetailDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  loading: boolean;
  recipe: Recipe | null;
  onToggleFavorite: (recipe: Recipe) => void;
}

export default function RecipeDetailDialog({
  open,
  onOpenChange,
  loading,
  recipe,
  onToggleFavorite,
}: RecipeDetailDialogProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      {loading ? (
        <RecipeDetailDialogSkeleton />
      ) : (
        <DialogContent>
          <DialogHeader>
            <div className="flex items-center gap-2">
              <DialogTitle>{recipe?.title}</DialogTitle>
              {recipe && (
                <button
                  type="button"
                  onClick={() => onToggleFavorite(recipe)}
                  aria-label={recipe.favorited ? 'Remove from favorites' : 'Add to favorites'}
                  aria-pressed={recipe.favorited}
                  className="text-muted-foreground hover:text-foreground"
                >
                  <Star
                    size={16}
                    className={recipe.favorited ? 'fill-current text-yellow-500' : ''}
                  />
                </button>
              )}
            </div>
            <DialogDescription className="text-xs">
              {recipe && `Created ${new Date(recipe.createdAt).toLocaleDateString()}`}
            </DialogDescription>
          </DialogHeader>
          <div className="flex flex-col gap-4">
            <div>
              <h3 className="mb-1.5 text-sm font-medium">Ingredients</h3>
              {recipe?.ingredients?.length ? (
                <ul className="list-inside list-disc text-sm text-muted-foreground">
                  {recipe.ingredients.map((ingredient, index) => (
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
                {recipe?.steps || 'No steps listed.'}
              </p>
            </div>
          </div>
        </DialogContent>
      )}
    </Dialog>
  );
}

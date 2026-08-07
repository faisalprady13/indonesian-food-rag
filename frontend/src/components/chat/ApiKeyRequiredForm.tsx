import { useRef, useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Input } from '@/components/ui/input.tsx';
import { Button } from '@/components/ui/button.tsx';
import { updateUserSetting } from '@/queries';
import type { AppTheme } from '@/types/UserSetting.ts';

const DEFAULT_APP_THEME: AppTheme = 'dark';

interface ApiKeyRequiredFormProps {
  appTheme?: AppTheme;
}

export default function ApiKeyRequiredForm({ appTheme }: ApiKeyRequiredFormProps) {
  const queryClient = useQueryClient();
  const apiKeyInputRef = useRef<HTMLInputElement>(null);
  const [apiKeyError, setApiKeyError] = useState<string | null>(null);

  const saveApiKeyMutation = useMutation({
    mutationFn: (apiKey: string) =>
      updateUserSetting({ appTheme: appTheme ?? DEFAULT_APP_THEME, apiKey }),
    onSuccess: () => {
      setApiKeyError(null);
      queryClient.invalidateQueries({ queryKey: ['user-setting'] });
    },
    onError: (error) => {
      console.error(error);
      setApiKeyError('Failed to save API key');
    },
  });

  function handleSubmit() {
    const apiKey = apiKeyInputRef.current?.value.trim() ?? '';
    if (!apiKey || saveApiKeyMutation.isPending) {
      return;
    }
    saveApiKeyMutation.mutate(apiKey);
  }

  return (
    <div className="mx-auto flex min-h-[calc(100svh-4.5rem)] w-full max-w-3xl flex-col items-center justify-center gap-4 p-8">
      <div className="flex w-full max-w-sm flex-col gap-2 text-center">
        <h2 className="text-lg font-semibold">An OpenAI API key is required</h2>
        <p className="text-sm text-muted-foreground">Add your OpenAI API key to start chatting.</p>
      </div>
      <form
        onSubmit={(e) => {
          e.preventDefault();
          handleSubmit();
        }}
        className="flex w-full max-w-sm flex-col gap-2"
      >
        <Input
          ref={apiKeyInputRef}
          type="text"
          placeholder="Enter your OpenAI API key"
          autoComplete="off"
          aria-invalid={!!apiKeyError}
          disabled={saveApiKeyMutation.isPending}
        />
        {apiKeyError && <p className="text-sm text-destructive">{apiKeyError}</p>}
        <Button type="submit" disabled={saveApiKeyMutation.isPending}>
          {saveApiKeyMutation.isPending ? 'Saving...' : 'Save API key'}
        </Button>
      </form>
    </div>
  );
}
